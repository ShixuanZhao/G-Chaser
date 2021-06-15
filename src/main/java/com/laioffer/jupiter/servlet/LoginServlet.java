package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.LoginRequestBody;
import com.laioffer.jupiter.entity.LoginResponseBody;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
// Read user data from the request body
//        ObjectMapper mapper = new ObjectMapper();
//        LoginRequestBody body = mapper.readValue(request.getReader(), LoginRequestBody.class);
        //ServletUtil.readRequestBody我们写的helper func转成我们需要的类型
        LoginRequestBody body = ServletUtil.readRequestBody(LoginRequestBody.class, request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String username = "";
        MySQLConnection connection = null;
        try {
            // Verify if the user ID and password are correct
            connection = new MySQLConnection();
            String userId = body.getUserId();
            //先转成MD5的形式
            String password = ServletUtil.encryptPassword(body.getUserId(), body.getPassword());
            username = connection.verifyLogin(userId, password);
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            connection.close();
        }

        // Create a new session for the user if user ID and password are correct, otherwise return Unauthorized error.
        if (!username.isEmpty()) {
            // Create a new session, put user ID as an attribute into the session object, and set the expiration time to 600 seconds.
            HttpSession session = request.getSession();
            session.setAttribute("user_id", body.getUserId());
            //空闲10min就要重新登录
            session.setMaxInactiveInterval(600);

            LoginResponseBody loginResponseBody = new LoginResponseBody(body.getUserId(), username);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(new ObjectMapper().writeValueAsString(loginResponseBody));
        } else {
            //没有登录成功，返回错误值，401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

    }
}
