package org.eclipse.epsilon.emc.html;

import org.eclipse.epsilon.emc.plainxml.PlainXmlProperty;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.introspection.java.JavaPropertySetter;
import org.jsoup.nodes.Element;

public class HtmlPropertySetter extends JavaPropertySetter {
	
	@Override
	public void invoke(Object value) throws EolRuntimeException {
		
		PlainXmlProperty p = PlainXmlProperty.parse(property);
		if (p!=null && p.isAttribute()) {
			Element element = (Element) object;
			element.attr(p.getProperty(), String.valueOf(value));
			return;
		}
		
		super.invoke(value);
	}
	
}
