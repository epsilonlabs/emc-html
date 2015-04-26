package org.eclipse.epsilon.emc.html;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

import javax.xml.ws.http.HTTPException;

import org.eclipse.epsilon.common.util.FileUtil;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.plainxml.PlainXmlType;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolEnumerationValueNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.exceptions.models.EolNotInstantiableModelElementTypeException;
import org.eclipse.epsilon.eol.execute.introspection.IPropertyGetter;
import org.eclipse.epsilon.eol.models.CachedModel;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

//TODO: Add support for timeout
public class HtmlModel extends CachedModel<org.jsoup.nodes.Element> {
	
	protected HtmlPropertyGetter propertyGetter = new HtmlPropertyGetter();
	protected HtmlPropertySetter propertySetter = new HtmlPropertySetter();
	protected HttpStatusException httpException;
 	protected Document document;
	protected final String ELEMENT_TYPE = "Element";
	public static String PROPERTY_FILE = "file";
	public static String PROPERTY_URI = "uri";
	public static String PROPERTY_TIMEOUT = "timeout";
	
	protected String uri = null;
	protected File file = null;
	protected int timeout = 60000;
	
	public static void main(String[] args) throws Exception {
		/*
		EolModule module = new EolModule();
		module.parse("t_div.all.size().println();");
		HtmlModel model = new HtmlModel();
		model.setName("M");
		model.setUri("http://www.google.com");
		model.load();
		module.getContext().getModelRepository().addModel(model);
		module.execute();*/
		
		HtmlModel m = new HtmlModel();
		m.setUri("https://github.com/search?page=5&utf8=%E2%9C%93&q=transform+extension%3Aetl&type=Code&ref=searchresults");
		m.setCachingEnabled(false);
		m.load();
		
		System.out.println(m.getAllOfType("t_p"));
		
		
	}
	
	@Override
	public void load(StringProperties properties, IRelativePathResolver resolver)
			throws EolModelLoadingException {
		super.load(properties, resolver);
		String fileProperty = properties.getProperty(PROPERTY_FILE);
		
		if (fileProperty != null && fileProperty.length() > 0) {
			file = new File(resolver.resolve(fileProperty));
		}
		else {
			uri = properties.getProperty(PROPERTY_URI);
			if (uri.startsWith("file:")) {
				try {
					file = new File(new URI(uri));
				} catch (URISyntaxException e) {
					throw new EolModelLoadingException(e, this);
				}
			}
		}
		load();
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	@Override
	public Object getEnumerationValue(String enumeration, String label)
			throws EolEnumerationValueNotFoundException {
		return null;
	}

	@Override
	public String getTypeNameOf(Object instance) {
		return "t_" + ((Element) instance).tagName();
	}

	@Override
	public Object getElementById(String id) {
		return null;
	}

	@Override
	public String getElementId(Object instance) {
		return null;
	}

	@Override
	public void setElementId(Object instance, String newId) {
		
	}

	@Override
	public boolean owns(Object instance) {
		if (instance instanceof Element) {
			return ((Element) instance).ownerDocument() == document;
		}
		else return false;
	}

	@Override
	public boolean isInstantiable(String type) {
		return true;
	}

	@Override
	public boolean hasType(String type) {
		return ELEMENT_TYPE.equals(type) || PlainXmlType.parse(type) != null;
	}

	@Override
	public boolean store(String location) {
		try {
			FileUtil.setFileContents(document.html(), new File(location));
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean store() {
		if (file != null) {
			store(file.getAbsolutePath());
			return true;
		}
		else {
			throw new UnsupportedOperationException("Cannot save to " + uri);
		}
	}

	@Override
	protected Collection<? extends Element> allContentsFromModel() {
		return document.getAllElements();
	}

	@Override
	protected Collection<? extends Element> getAllOfTypeFromModel(String type)
			throws EolModelElementTypeNotFoundException {
		return document.select(PlainXmlType.parse(type).getTagName());
	}

	@Override
	protected Collection<? extends Element> getAllOfKindFromModel(String kind)
			throws EolModelElementTypeNotFoundException {
		return getAllOfTypeFromModel(kind);
	}

	@Override
	protected Element createInstanceInModel(String type)
			throws EolModelElementTypeNotFoundException,
			EolNotInstantiableModelElementTypeException {
		return document.createElement(PlainXmlType.parse(type).getTagName());
	}

	@Override
	protected void loadModel() throws EolModelLoadingException {
		
		if (readOnLoad) {
			try {
				if (file != null) {
					document = Jsoup.parse(file, null);
				}
				else {
					Connection connection = Jsoup.connect(uri);
					connection.timeout(timeout);
					document = connection.get();
				}
			} 
			catch (HttpStatusException ex) {
				document = Document.createShell(uri);
				httpException = ex;
			}
			catch (IOException e) {
				throw new EolModelLoadingException(e, this);
			}
		}
		else {
			String baseUri = null;
			if (file != null) {
				baseUri = file.toURI().toString();
			}
			else if (uri != null) {
				baseUri = uri;
			}
			else {
				baseUri = "";
			}
			document = Document.createShell(baseUri);
		}
	}

	@Override
	protected void disposeModel() {
		httpException = null;
	}

	@Override
	protected boolean deleteElementInModel(Object instance)
			throws EolRuntimeException {
		((Element) instance).remove();
		return false;
	}

	@Override
	protected Object getCacheKeyForType(String type)
			throws EolModelElementTypeNotFoundException {
		return type;
	}

	@Override
	protected Collection<String> getAllTypeNamesOf(Object instance) {
		return Collections.singleton(getTypeNameOf(instance));
	}
	
	public Document getDocument() {
		return document;
	}
	
	@Override
	public IPropertyGetter getPropertyGetter() {
		return propertyGetter;
	}
	
	@Override
	public HtmlPropertySetter getPropertySetter() {
		return propertySetter;
	}
	
	public HttpStatusException getHttpException() {
		return httpException;
	}

}
