package org.motorbrot.javafxjcrbrowser.services;

import java.io.InputStream;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.testing.mock.jcr.MockJcr;

/**
 * uses sling.testing.mock.jcr.MockJcr
 */
public class JcrServiceMock extends JcrService {

  @Override
  public void login(String repo, String user, String passwd, String workspace) throws RepositoryException {
    
    Session jcrSession = MockJcr.newSession();
    getJcrSessionProperty().set(jcrSession);
    
    // load a csv binary
    Node csvNode = JcrUtils.getOrCreateByPath("/content/testfx/csv", null, jcrSession);
    csvNode.setProperty("sling:resourceType", "javafxjcrbrowser:sample");
    
    ValueFactory factory = jcrSession.getValueFactory();
    InputStream is = getClass().getResourceAsStream("/testdata/exampleFxTable.csv");
    Binary binary = factory.createBinary(is);
    csvNode.setProperty("csvBlob", binary);
    
  }
  
}
