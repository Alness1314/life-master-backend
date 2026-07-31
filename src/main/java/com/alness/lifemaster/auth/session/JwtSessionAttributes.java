package com.alness.lifemaster.auth.session;

public final class JwtSessionAttributes {
    public static final String TOKEN_ID = JwtSessionAttributes.class.getName() + ".tokenId";
    public static final String EXPIRES_AT = JwtSessionAttributes.class.getName() + ".expiresAt";

    private JwtSessionAttributes() {
    }
}
