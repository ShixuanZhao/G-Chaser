package com.laioffer.jupiter.servlet;

import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.recommendation.ItemRecommender;
import com.laioffer.jupiter.recommendation.RecommendationException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "RecommendationServlet", value = "/recommendation")
public class RecommendationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //先需要认证，不让他创建新的session
        HttpSession session = request.getSession(false);
        ItemRecommender itemRecommender = new ItemRecommender();
        Map<String, List<Item>> itemMap;

        // If the user is successfully logged in, recommend by the favorite records,
        // otherwise recommend by the top games.
        try {
            //user没有登录
            if (session == null) {
                itemMap = itemRecommender.recommendItemsByDefault();
            } else {
                String userId = (String) request.getSession().getAttribute("user_id");
                itemMap = itemRecommender.recommendItemsByUser(userId);
            }
        } catch (RecommendationException e) {
            throw new ServletException(e);
        }
        //返回给前端，需要我们的helper func
        ServletUtil.writeItemMap(response, itemMap);
    }
//查询不需要post，不需要对数据库进行修改
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//    }
}
