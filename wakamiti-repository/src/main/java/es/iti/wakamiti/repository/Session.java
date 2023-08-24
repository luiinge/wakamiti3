package es.iti.wakamiti.repository;

import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.*;
import java.util.stream.*;

import es.iti.wakamiti.api.WakamitiException;

public class Session implements AutoCloseable {

	private static final Pattern PARAM_REGEX = Pattern.compile("\\?\\d\\d?");

	@FunctionalInterface
	interface Mapper<T> {
		T mapThrowing(ResultSet resultSet) throws Exception;
		default T map (ResultSet resultSet) {
			try {
				return mapThrowing(resultSet);
			} catch (Exception e) {
				throw new WakamitiException(e);
			}
		}
	}

	private final Supplier<Connection> connectionProvider;
	private final Map<String, PreparedStatement> statements = new HashMap<>();
	private Connection connection;

	public Session(Supplier<Connection> connectionProvider) {
		this.connectionProvider = connectionProvider;
	}


	public void commit() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.commit();
			}
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}


	@Override
	public void close() throws SQLException {
		for (PreparedStatement statement : statements.values()) {
			if (!statement.isClosed()) {
				statement.close();
			}
		}
		if (connection != null && !connection.isClosed()) {
			connection.close();
		}
	}


	private Connection connection() {
		try {
			if (connection == null || connection.isClosed()) {
				statements.clear();
				connection = connectionProvider.get();
				connection.setAutoCommit(false);
			}
			return connection;
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}


	private PreparedStatement statement(String sql, Object... args) {
		try {
			// replace the ?1, ?2, etc. by simple ? and create the actual parameter
			List<Object> actualArgs = new LinkedList<>();
			Matcher paramMatcher = PARAM_REGEX.matcher(sql);
			while (paramMatcher.find()) {
				int index = Integer.parseInt(paramMatcher.group().substring(1));
				actualArgs.add(args[index]);
			}
			String actualSql = PARAM_REGEX.matcher(sql).replaceAll("?");

			PreparedStatement cached = statements.get(actualSql);
			if (cached == null || cached.isClosed()) {
				cached = connection().prepareStatement(actualSql);
				statements.put(actualSql, cached);
			}
			for (int i = 0; i < actualArgs.size(); i++) {
				cached.setObject(i+1, actualArgs.get(i));
			}

			return cached;
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}


	public <T> Stream<T> stream(Mapper<T> mapper, String sql, Object... args) {
		try (var resultSet = statement(sql,args).executeQuery()) {
			return stream(resultSet,mapper);
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}


	public <T> List<T> list(Mapper<T> mapper, String sql, Object... args) {
		try (var resultSet = statement(sql,args).executeQuery()) {
			List<T> result = new LinkedList<>();
			while (resultSet.next()) {
				result.add(mapper.map(resultSet));
			}
			return List.copyOf(result);
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}


	public <T> Optional<T> optional(Mapper<T> mapper, String sql, Object... args) {
		try (var resultSet = statement(sql,args).executeQuery()) {
			return resultSet.next() ? Optional.of(mapper.map(resultSet)) : Optional.empty();
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}


	public <T> T single(Mapper<T> mapper, String sql, Object... args) {
		try (var resultSet = statement(sql,args).executeQuery()) {
			resultSet.next();
			return mapper.map(resultSet);
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}

	public boolean exists(String sql, Object... args) {
		try (var resultSet = statement(sql,args).executeQuery()) {
			return resultSet.next();
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}


	public void execute(List<String> sentences) {
		try (var statement = connection().createStatement()) {
			for (String sentence : sentences) {
				statement.addBatch(sentence);
			}
			statement.executeBatch();
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}


	public void execute(String sql, Object... args) {
		try {
			statement(sql,args).executeUpdate();
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}




	private <T> Stream<T> stream(ResultSet resultSet, Mapper<T> mapper) {
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(
				new ResultSetIterator<>(resultSet, mapper),
				Spliterator.ORDERED
			), false)
		.onClose(() -> {
			try {
				resultSet.close();
			} catch (SQLException e) {
				throw new WakamitiException(e);
			}
		});
	}


	private record ResultSetIterator<T>(ResultSet resultSet, Mapper<T> mapper)
	implements Iterator<T> {

		@Override
		public boolean hasNext() {
			try {
				return !resultSet.isLast();
			} catch (SQLException e) {
				throw new WakamitiException(e);
			}
		}

		@Override
		public T next() {
			try {
				resultSet.next();
				return mapper.map(resultSet);
			} catch (SQLException e) {
				throw new WakamitiException(e);
			}
		}

		@Override
		public void remove() {
			try {
				resultSet.deleteRow();
			} catch (SQLException e) {
				throw new WakamitiException(e);
			}
		}

	}



}
