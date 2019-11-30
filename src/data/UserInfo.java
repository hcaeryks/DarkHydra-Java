package data;

public final class UserInfo {
    public static int idUsuario;
    public static String picPath;
    public static String login;
    public static String senha;

    public static int getIdUsuario() {
        return idUsuario;
    }

    public static void setIdUsuario(int idUsuario) {
        UserInfo.idUsuario = idUsuario;
    }
    
    public static String getPicPath() {
        return picPath;
    }

    public static void setPicPath(String picPath) {
        UserInfo.picPath = picPath;
    }

    public static String getLogin() {
        return login;
    }

    public static void setLogin(String login) {
        UserInfo.login = login;
    }

    public static String getSenha() {
        return senha;
    }

    public static void setSenha(String senha) {
        UserInfo.senha = senha;
    }
}
