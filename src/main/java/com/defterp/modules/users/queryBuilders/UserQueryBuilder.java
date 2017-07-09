package com.defterp.modules.users.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class UserQueryBuilder {

    private static final String FIND_ALL = "SELECT u FROM User u";
    private static final String FIND_BY_LOGIN = "SELECT u FROM User u WHERE u.login = :login AND u.password = :password AND u.active = true";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }

    public static QueryWrapper getUserExistQuery(String userName, String password) {

        return new QueryWrapper(FIND_BY_LOGIN)
                .setParameter("login", userName)
                .setParameter("password", password);
    }
}
