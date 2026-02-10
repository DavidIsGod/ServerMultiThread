package edu.co.icesi.servlets;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/function2")
public class SumServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // http://localhost:8080/function2?alfa=11&beta=14
        int alfa = Integer.parseInt(req.getParameter("alfa"));
        int beta = Integer.parseInt(req.getParameter("beta"));
        resp.setContentType("text/html");
        resp.getWriter().println("<h1>La suma es "+(alfa+beta)+"</h1>");
    }

}
