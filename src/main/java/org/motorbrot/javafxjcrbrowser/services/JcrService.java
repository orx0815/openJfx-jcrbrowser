package org.motorbrot.javafxjcrbrowser.services;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.jackrabbit.commons.JcrUtils;

/**
 * Spring bean for jcr access
 */
public class JcrService {

  private static final Logger LOG = Logger.getLogger(JcrService.class.getName());
  
  private final ObjectProperty<Session> session = new SimpleObjectProperty<>(null);
  
  /**
   * @return actual session, or null
   */
  public Session getJcrSession() {
    return session.get();
  }

  /**
   * @return binding-property of the session
   */
  public ObjectProperty<Session> getJcrSessionProperty() {
    return session;
  }
  
  /**
   * does the login into jcr
   * 
   * @param repo url 
   * @param user the jcr user
   * @param passwd the jcr password
   * @param workspace the workspace in jcr
   * @throws RepositoryException when it fails
   */
  public void login(String repo, String user, String passwd, String workspace) throws RepositoryException {
      Repository repository = JcrUtils.getRepository(repo);
      SimpleCredentials crxCredentials = new SimpleCredentials(user, passwd.toCharArray());
      Session jcrSession = repository.login(crxCredentials, workspace);
      session.set(jcrSession);
  }

  /**
   * LOGOUT
   */
  public void logout() {
    if (session.getValue() != null) {
      LOG.log(Level.INFO, "Logging out of jcr-Session ...");
      session.getValue().logout();
      session.set(null);
    }
  }

  /**
   * SAVE SESSION
   * @throws RepositoryException when it fails
   */
  public void save() throws RepositoryException {
    LOG.log(Level.INFO, "Saving jcr-Session ...");
    session.get().save();
    LOG.log(Level.INFO, "Jcr-Session saved.");
  }
  
}
