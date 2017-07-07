package com.defterp.modules.users.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class UserQueryBuilder {

    private static final String FIND_ALL = "SELECT u FROM User u";
    private static final String FIND_BY_LOGIN = "SELECT u FROM User u WHERE u.login = :login AND u.password = :password AND u.active = true";

    private static QueryWrapper query;

    public static QueryWrapper getFindAllQuery() {
        query = new QueryWrapper(FIND_ALL);

        return query;
    }

    public static QueryWrapper getFindByLoginQuery(String userName, String password) {
        query = new QueryWrapper(FIND_BY_LOGIN)
                .setParameter("login", userName)
                .setParameter("password", password);

        return query;
    }
}
