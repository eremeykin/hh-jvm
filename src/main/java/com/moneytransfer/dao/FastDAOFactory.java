package com.moneytransfer.dao;

import com.moneytransfer.dao.impl.AccountDAOImpl;
import com.moneytransfer.dao.impl.UserDAOImpl;
import com.moneytransfer.dao.impl.fast.FastAccountDAOImpl;
import com.moneytransfer.dao.impl.fast.FastUserDAOImpl;
import com.moneytransfer.exception.CustomException;
import com.moneytransfer.model.Account;
import com.moneytransfer.model.User;
import com.moneytransfer.utils.Utils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.h2.tools.RunScript;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * H2 DAO
 */
public class FastDAOFactory extends DAOFactory {
	private static Logger log = Logger.getLogger(FastDAOFactory.class);

	private static final FastUserDAOImpl userDAO = new FastUserDAOImpl();
	private static final FastAccountDAOImpl accountDAO = new FastAccountDAOImpl();

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public AccountDAO getAccountDAO() {
		return accountDAO;
	}

	@Override
	public void populateTestData() throws CustomException {
		log.info("Populating Test User Table and data using FastDAOFactory ..... ");
		userDAO.dropTable();
		userDAO.insertUser(new User(1, "test2","test2@gmail.com"));
		userDAO.insertUser(new User(2, "test1","test1@gmail.com"));
		userDAO.insertUser(new User(3, "yangluo","yangluo@gmail.com"));
		userDAO.insertUser(new User(4, "qinfran","qinfran@gmail.com"));
		userDAO.insertUser(new User(5, "liusisi","liusisi@gmail.com"));

		accountDAO.dropTable();
		accountDAO.createAccount(new Account(1,"yangluo", new BigDecimal(100), "USD"));
		accountDAO.createAccount(new Account(2,"qinfran", new BigDecimal(200), "USD"));
		accountDAO.createAccount(new Account(3,"yangluo", new BigDecimal(500), "EUR"));
		accountDAO.createAccount(new Account(4,"qinfran", new BigDecimal(500), "EUR"));
		accountDAO.createAccount(new Account(5,"yangluo", new BigDecimal(500), "GBP"));
		accountDAO.createAccount(new Account(6,"qinfran", new BigDecimal(500), "GBP"));
	}
}
