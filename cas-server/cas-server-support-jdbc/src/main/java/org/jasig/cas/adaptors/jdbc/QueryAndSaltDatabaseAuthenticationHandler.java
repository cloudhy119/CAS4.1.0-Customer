/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.jdbc;

import java.security.GeneralSecurityException;
import java.util.Map;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * Class that if provided a query that returns a password (parameter of query
 * must be username) will compare that password to a translated version of the
 * password provided by the user. If they match, then authentication succeeds.
 * Default password translator is plaintext translator.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 *
 * @since 3.0
 */
public class QueryAndSaltDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {
    private static final String DEFAULT_PASSWORD_FIELD = "password";
//    private static final String DEFAULT_SALT_FIELD = "salt";
    private static final long DEFAULT_ITERATIONS = 1024;

    /**
     * The Algorithm name.
     */
//    @NotNull
//    protected final String algorithmName;

    /**
     * The Sql statement to execute.
     */
    @NotNull
    protected final String sql;

    /**
     * The Sql statement to execute.
     */
//    @NotNull
//    protected final String updateSql;

    /**
     * The Password field name.
     */
    @NotNull
    protected String passwordFieldName = DEFAULT_PASSWORD_FIELD;

    /**
     * The Salt field name.
     */
//    @NotNull
//    protected String saltFieldName = DEFAULT_SALT_FIELD;

    /**
     * The number of iterations. Defaults to 0.
     */
    protected Long numberOfIterations = DEFAULT_ITERATIONS;


    public QueryAndSaltDatabaseAuthenticationHandler(final DataSource dataSource,
                                                       final String sql) {
        super();
        setDataSource(dataSource);
        this.sql = sql;
    }

    /** {@inheritDoc} */
    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {
        final String username = credential.getUsername();
        final String encodedPsw = this.getPasswordEncoder().encode(credential.getPassword());
        try {
            final Map<String, Object> values = getJdbcTemplate().queryForMap(this.sql, username);
            //效验密码
            if(!validatePassword(encodedPsw, values.get(this.passwordFieldName).toString())) {
            	throw new FailedLoginException("Password does not match value on record.");
            }
        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            } else {
                throw new FailedLoginException("Multiple records found for " + username);
            }
        } catch (final DataAccessException e) {
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
    }

	/**
	 * 验证密码
	 * @param plainPassword 明文密码
	 * @param password 密文密码
	 * @return 验证成功返回true
	 */
	public static boolean validatePassword(String plainPassword, String password) {
		String plain = Encodes.unescapeHtml(plainPassword);
		byte[] salt = Encodes.decodeHex(password.substring(0,16));
		byte[] hashPassword = Digests.sha1(plain.getBytes(), salt, (int) DEFAULT_ITERATIONS);
		return password.equals(Encodes.encodeHex(salt)+Encodes.encodeHex(hashPassword));
	}

    /**
     * Sets password field name. Default is {@link #DEFAULT_PASSWORD_FIELD}.
     *
     * @param passwordFieldName the password field name
     */
    public final void setPasswordFieldName(final String passwordFieldName) {
        this.passwordFieldName = passwordFieldName;
    }

    /**
     * Sets salt field name. Default is {@link #DEFAULT_SALT_FIELD}.
     *
     * @param saltFieldName the password field name
     */
//    public final void setSaltFieldName(final String saltFieldName) {
//        this.saltFieldName = saltFieldName;
//    }

    /**
     * Sets number of iterations. Default is 0.
     *
     * @param numberOfIterations the number of iterations
     */
    public final void setNumberOfIterations(final Long numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }
}