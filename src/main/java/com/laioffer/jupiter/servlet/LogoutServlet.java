package com.laioffer.jupiter.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "LogoutServlet", value = "/logout")
public class LogoutServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
// Destroy the session since the user is logged out.
        HttpSession session = request.getSession(false);
        //如果session有效，让他无效
        if (session != null) {
            session.invalidate();
        }
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        //有效期设置0，清除用户端的cookie
        cookie.setMaxAge(0);
        response.addCookie(cookie);

    }
}
