package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.shortcut;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.link.Work;
import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.link.WorkType;
import eu.unifiedviews.dpu.DPUContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 
 * @author Jakub Starka
 */
public class ShortcutMap extends HashMap<String, Work> {


	private static String normalize(String word) {
		return word.toLowerCase().replaceAll("\\.", "").trim();
	}

	private LinkedList<String> find(String shortcut) {

		LinkedList<String> output = new LinkedList<>();

		String s = shortcut.toLowerCase().replaceAll("zákona", "")
				.replaceAll("zákoníku", "").replaceAll("zákoník", "")
				.replaceAll("zákon", "").replaceAll("zák.", "")
				.replaceAll("\\.", " ").replaceAll("  +", " ").trim();

		for (Map.Entry<String, Work> entry : super.entrySet()) {
			// check exact match
			if (entry.getKey().equals(s)) {
				output.add(entry.getKey());
			}

			// split both by spaces and compare
			String[] entryWords = entry.getKey().toLowerCase()
					.replaceAll("zákona", "").replaceAll("zákoníku", "")
					.replaceAll("zákoník", "").replaceAll("zákon", "").trim()
					.replaceAll("  +", " ").split(" ");
			String[] searchWords = s.split(" ");

			if (searchWords.length == 1) {
				searchWords = searchWords[0].split("\\.");
			}

			// a ještě jednou ZVZ => Z V Z (toChar = > stringy)

			// if word count do not match
			if (entryWords.length != searchWords.length) {
				continue;
			}

			boolean match = true;

			// lower case, remove dots and compare
			for (int i = 0; i < entryWords.length; i++) {
				if (ShortcutMap.normalize(searchWords[i]).isEmpty()) {
					match = false;
					break;
				}
				if (!ShortcutMap.normalize(entryWords[i]).startsWith(
						ShortcutMap.normalize(searchWords[i]))) {
					match = false;
					break;
				}
			}

			if (match) {
				output.add(entry.getKey());
			}
		}

		return output;
	}

	@Override
	public boolean containsKey(Object o) {
		if (!(o instanceof String)) {
			return super.containsKey(o);
		}

		String s = (String) o;

		LinkedList<String> output = this.find(s);

		if (output.size() == 0) {
			return false;
		} else if (output.size() == 1) {
			return true;
		} else {
			// Logger.getLogger(ShortcutMap.class.getName()).log(Level.INFO,
			// "Shortcut collision {0} - returning first match: {1} Searching: {2}",
			// new Object[]{output.toString(), output.getFirst(), o});
			return true;
		}
	}

	@Override
	public Work get(Object o) {
		if (!(o instanceof String)) {
			return super.get(o);
		}

		String s = (String) o;

		LinkedList<String> output = this.find(s);

		if (output.size() == 0) {
			return null;
		} else if (output.size() == 1) {
			return super.get(output.get(0));
		} else {
			// Logger.getLogger(ShortcutMap.class.getName()).log(Level.INFO,
			// "Shortcut collision {0} - returning first match: {1} Searching: {2}",
			// new Object[]{output.toString(), output.getFirst(), o});
			return super.get(output.get(0));
		}
	}

	public void load(DPUContext context)
			throws FileNotFoundException,
				IOException {

		// set the tempDir
		if (context != null) {
			SparqlLoader.tempDir = context.getWorkingDir();

		} else {
			SparqlLoader.tempDir = new File(
					System.getProperty("java.io.tmpdir"));
		}

		FileReader fr = new FileReader(
				SparqlLoader.getFile(SparqlLoader.ACT_LIST));
		BufferedReader br = new BufferedReader(fr);
		String s;
		int added = 0;
		int skipped = 0;
		while ((s = br.readLine()) != null) {
			if (!s.contains("/")) {
				continue;
			}
			String[] parts = s.split("\",\"");

			String number = parts[2].replaceAll("\"", "");
			// LinkedList<Work> w = Work.parse(number, WorkType.ACT);
			String[] numberParts = number.split("/");
			Work w = new Work("", "cz", WorkType.ACT,
					Integer.parseInt(numberParts[1]), numberParts[0], null,
					null, null, null, null, null);

			if (parts[1].contains("kter") || parts[1].contains("jímž")
					|| parts[1].toLowerCase().contains("nález")
					|| parts[1].toLowerCase().contains("úplné znění")) {
				skipped++;
			} else {
				super.put(parts[1], w);
				added++;
			}
		}
		System.out.println("Generic shortcuts");
		System.out.println("Added: " + added);
		System.out.println("Skipped: " + skipped);

	}


}
