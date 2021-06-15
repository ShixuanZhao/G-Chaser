package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

//如果postman 404，就是endpoint找不到，问题就是endpoint注册的和请求的不一样，或者忘记redeploytomcat
//500就是网络错误，代码是对的
@WebServlet(name = "SearchServlet", value = "/search")
public class SearchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String gameId = request.getParameter("game_id");//符合Json的格式
        if (gameId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        TwitchClient client = new TwitchClient();
        try {
            //jackson将java class转成json在转成前端显示的string
            ServletUtil.writeItemMap(response, client.searchItems(gameId));
        } catch (TwitchException e) {
            throw new ServletException(e);
        }

    }


}
