package com.adam.medipathbackend.config;

public class Constants {
    public static final String GENERIC_MAIL_FORMAT = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\" />\n" +
            "    <title>Medipath</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h2>%s</h2>\n" +
            "    %s\n" +
            "    <p>-MediPath development team</p>\n" +
            "    \n" +
            "</body>\n" +
            "</html>";

    public static final String EMPLOYEE_REGISTRATION_MAIL_FORMAT_EN = "<p>An administrator for <strong>%s</strong>" +
            " has created an account for you in the Medipath system. Use the link below to set a new password.</p>\n" +
            "    <a href=\"http://localhost:4200/auth/resetpassword/%s" +
            "\">http://localhost:4200/auth/resetpassword/%s</a>\n" +
            "    <br>\n" +
            "    <p>The link will expire within 24 hours</p>\n" +
            "    <p>If you have not sent a password reset request, ignore this email.</p>";

    public static final String VISIT_CANCELLATION_MAIL_FORMAT_EN = "<p>Your visit in <strong>%s</strong>" +
            " at <strong>%s</strong> with <strong>%s</strong> has been cancelled. <br> For further information, contact the institution.</p>";

    public static final String VISIT_RESCHEDULING_MAIL_FORMAT_EN = "<p>Your visit in <strong>%s</strong>" +
            " on <strong>%s</strong> <with <strong>%s</strong> has been rescheduled. <br> The new visit will be held in " +
            "<strong>%s</strong> on <strong>%s</strong> with <strong>%s</strong>. " +
            "<br> For further information, contact the institution.</p>";

    public static final String PASSWORD_RESET_MAIL_FORMAT_EN = "<p>We have received a password reset request for your account." +
            " Use the link below to set a new password.</p>\n" +
            "    <a href=\"http://localhost:4200/auth/resetpassword/%s" +
            "\">http://localhost:4200/auth/resetpassword/%s</a>\n" +
            "    <br>\n" +
            "    <p>The link will expire within 24 hours</p>";

    public static final String PASSWORD_RESET_SUCCESS_MAIL_FORMAT_EN = "<p>Your Medipath password has been reset." +
            " If you have not reset your password, change your password via the \"Forgot password\" form, as your account" +
            "may be compromised.</p>";

    public static final String EMPLOYEE_REGISTRATION_MAIL_FORMAT_PL = "<p>Administrator placówki <strong>%s</strong>" +
            " utworzył dla ciebie konto w systemie Medipath. Użyj odnośnika poniżej żeby ustawić nowe hasło.</p>\n" +
            "    <a href=\"http://localhost:4200/auth/resetpassword/%s" +
            "\">http://localhost:4200/auth/resetpassword/%s</a>\n" +
            "    <br>\n" +
            "    <p>Powyższy link straci ważność za 24 godziny</p>\n" +
            "    <p>Jeśli nie wysyłałeś prośby o reset hasła, zignoruj ten mail lub ustaw nowe hasło.</p>";

    public static final String VISIT_CANCELLATION_MAIL_FORMAT_PL = "<p>Twoja wizyta w placówce %s" +
            " <br> dnia %s u specjalisty %s została odwołana. <br> Skontaktuj się z placówką by uzyskać więcej informacji.</p>";

    public static final String VISIT_RESCHEDULING_MAIL_FORMAT_PL = "<p>Twoja wizyta w placówce <strong>%s</strong>" +
            " dnia <strong>%s</strong> u specjalisty <strong>%s</strong> została przełożona. <br> Nowa wizyta odbędzie się w placówce " +
            "<strong>%s</strong> dnia <strong>%s</strong> u specjalisty <strong>%s</strong>. " +
            "<br>Skontaktuj się z placówką by uzyskać więcej informacji.</p>";

    public static final String PASSWORD_RESET_MAIL_FORMAT_PL = "<p>Otrzymaliśmy prośbę o zmianę hasła do twojego konta Medipath." +
            " Użyj odnośnika poniżej żeby zmienić hasło.</p>\n" +
            "    <a href=\"http://localhost:4200/auth/resetpassword/%s" +
            "\">http://localhost:4200/auth/resetpassword/%s</a>\n" +
            "    <br>\n" +
            "    <p>Powyższy link straci ważność za 10 minut.</p>\n" +
            "    <p>Jeśli nie wysyłałeś prośby o reset hasła, zignoruj ten mail lub ustaw nowe hasło..</p>";

    public static final String PASSWORD_RESET_SUCCESS_MAIL_FORMAT_PL = "<p>Hasło do twojego konta Medipath zostało " +
            "zmienione. Jeśli nie zmieniałeś hasła, zmień na formularzu \"Zapomniałem hasła\", ponieważ twoje konto może" +
            "być zagrożone.</p>";


    public static final String VISIT_REMINDER_FORMAT_PL = "Przypominamy o wizycie w ośrodku %s dnia %s o godzinie %s";

    public static final String VISIT_REMINDER_FORMAT_EN =
            "We would like to remind you of your upcoming visit in %s on the day %s at %s";

    public static final String VISIT_REMINDER_TITLE_PL = "Przypomnienie o wizycie";

    public static final String VISIT_REMINDER_TITLE_EN = "Visit reminder";
}
