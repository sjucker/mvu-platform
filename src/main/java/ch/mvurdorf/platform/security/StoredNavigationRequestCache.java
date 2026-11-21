package ch.mvurdorf.platform.security;

import com.vaadin.flow.server.auth.NavigationAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;

/**
 * Exposes the view a user tried to open before being sent to the login view as saved request.
 * <p>
 * Vaadin's {@link NavigationAccessControl} stores that target in a session attribute, which only Vaadin's form login
 * success handler reads. The passkey login uses Spring's JSON success handler, which only consults the
 * {@link RequestCache}; without this it would always redirect to the root.
 */
class StoredNavigationRequestCache implements RequestCache {

    private static final String ATTRIBUTE = NavigationAccessControl.SESSION_STORED_REDIRECT_ABSOLUTE;

    private final RequestCache delegate = new HttpSessionRequestCache();

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        delegate.saveRequest(request, response);
    }

    @Override
    public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession(false);
        if (session != null && session.getAttribute(ATTRIBUTE) instanceof String redirectUrl) {
            return new SimpleSavedRequest(redirectUrl);
        }
        return delegate.getRequest(request, response);
    }

    @Override
    public HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response) {
        return delegate.getMatchingRequest(request, response);
    }

    @Override
    public void removeRequest(HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(ATTRIBUTE);
        }
        delegate.removeRequest(request, response);
    }
}
