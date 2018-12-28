package com.moneytransfer;

import com.moneytransfer.dao.DAOFactory;
import com.moneytransfer.service.AccountService;
import com.moneytransfer.service.ServiceExceptionMapper;
import com.moneytransfer.service.TransactionService;
import com.moneytransfer.service.UserService;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Main Class (Starting point) 
 */
public class Application {

	private static Logger log = Logger.getLogger(Application.class);

	public static void main(String[] args) throws Exception {
		// Initialize H2 database with demo data
		log.info("Initialize demo .....");
		DAOFactory h2DaoFactory = DAOFactory.getDAOFactory(DAOFactory.FAST);
		h2DaoFactory.populateTestData();
		log.info("Initialisation Complete....");
		// Host service on jetty
		startService();
	}

	private static void startService() throws Exception {
		Server server = new Server(new QueuedThreadPool(6, 1));
		ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory());
		connector.setPort(8080);
		server.addConnector(connector);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");
		servletHolder.setInitParameter("jersey.config.server.provider.classnames",
				UserService.class.getCanonicalName() + "," + AccountService.class.getCanonicalName() + ","
						+ ServiceExceptionMapper.class.getCanonicalName() + ","
						+ TransactionService.class.getCanonicalName());
		try {
			server.start();
			server.join();
		} finally {
			server.destroy();
		}
	}
}
