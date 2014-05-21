package cz.opendata.linked.saxon.extensions;


import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;


public class UUIDGenerator extends ExtensionFunctionDefinition {


	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName("uuid","http://linked.opendata.cz/xslt-functions", "randomUUID");
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[0];
	}

	@Override
	public SequenceType getResultType(SequenceType[] sequenceTypes) {
		return SequenceType.SINGLE_STRING;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {

			@Override
			public SequenceIterator call(SequenceIterator[] sequenceIterators, XPathContext xPathContext) throws XPathException {

				Item item = new StringValue(java.util.UUID.randomUUID().toString());

				return SingletonIterator.makeIterator(item);
			}

		};
	}



}
