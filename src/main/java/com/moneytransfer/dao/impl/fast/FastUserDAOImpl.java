package com.moneytransfer.dao.impl.fast;

import com.moneytransfer.dao.UserDAO;
import com.moneytransfer.exception.CustomException;
import com.moneytransfer.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FastUserDAOImpl implements UserDAO {

	private Map<Long, User> userRepository = new ConcurrentHashMap<>();
	private Map<String, Long> userNamesIndex = new ConcurrentHashMap<>();
	private long index = 0L;

	@Override
	public List<User> getAllUsers() throws CustomException {
		return new ArrayList<>(userRepository.values());
	}

	@Override
	public User getUserById(long userId) throws CustomException {
		return userRepository.get(userId);
	}

	@Override
	public User getUserByName(String userName) throws CustomException {
		Long index = userNamesIndex.get(userName);
		if (index==null){
			return null;
		}
		return userRepository.get(index);
	}

	@Override
	public long insertUser(User user) throws CustomException {
		userRepository.put(++index, user);
		userNamesIndex.put(user.getUserName(), user.getUserId());
		return index;
	}

	@Override
	public int updateUser(Long userId, User user) throws CustomException {
		if (userRepository.containsKey(userId)) {
			userRepository.put(userId, user);
			return 1;
		}
		return 0;
	}

	@Override
	public int deleteUser(long userId) throws CustomException {
		User prev = userRepository.remove(userId);
		if (prev == null) {
			return 0;
		}
		userNamesIndex.remove(prev.getUserName());
		return 1;
	}

	public void dropTable(){
		userRepository = new ConcurrentHashMap<>();
		userNamesIndex = new ConcurrentHashMap<>();
		index = 0L;
	}
}
