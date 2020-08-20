package com.dbapp.app.ai.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@WebListener
public class MirrorHttpSessionListener implements HttpSessionListener, HttpSessionAttributeListener, HttpSessionActivationListener {
    public static final Logger logger = LoggerFactory.getLogger(MirrorHttpSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        logger.debug("---sessionCreated---->{}", session.getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        logger.debug("---sessionDestroyed---->{}", session.getId());
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent se) {
        String name = se.getName();
        Object value = se.getValue();
        logger.debug("--attributeAdded---key--->{}", name);
        logger.debug("--attributeAdded---value--->{}", value);

    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent se) {
        String name = se.getName();
        Object value = se.getValue();
        logger.debug("--attributeRemoved---key--->{}", name);
        logger.debug("--attributeRemoved---value--->{}", value);
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent se) {
        String name = se.getName();
        Object value = se.getValue();
        logger.debug("--attributeRemoved---key--->{} ", name);
        logger.debug("--attributeRemoved---value--->{} ", value);
    }

    @Override
    public void sessionWillPassivate(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        List<String> attrs = Collections.list(session.getAttributeNames());
        logger.debug("---sessionWillPassivate---->{}", session.getId());
        logger.debug("with attributes: {}", Arrays.toString(attrs.toArray()));
    }

    @Override
    public void sessionDidActivate(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        List<String> attrs = Collections.list(session.getAttributeNames());
        logger.debug("---sessionDidActivate---->{}", session.getId());
        logger.debug("with attributes: {}", Arrays.toString(attrs.toArray()));
    }
}
