/**
  Copyright 2010 Anthony Chaves
  
  This file is part of Bookmarks.

  Bookmarks is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Bookmarks is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Bookmarks.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.anthonychaves.bookmarks.web;

import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.persistence.*;

import java.io.*;
import java.util.*;

import net.anthonychaves.bookmarks.models.*;
import net.anthonychaves.bookmarks.service.*;

@Component
public class PersistentLoginFilter implements Filter {

  @Autowired
  TokenService tokenService;
  
  @Override
  public void init(FilterConfig config) {
    // nothing for now
  }
  
  @Override
  public void doFilter(ServletRequest request, 
                       ServletResponse response, 
                       FilterChain chain) throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    Cookie tokenCookie = getCookieByName(httpRequest.getCookies(), "loginToken");

    HttpSession session = httpRequest.getSession();
    User user = (User) session.getAttribute("user");

    if (user == null && tokenCookie != null) {
      user = tokenService.loginWithToken(tokenCookie.getValue());
      String tokenValue = tokenService.setupNewLoginToken(user);
      
      httpRequest.getSession().setAttribute("user", user);
      tokenCookie.setMaxAge(0);
      httpResponse.addCookie(tokenCookie);
      
      tokenCookie = new Cookie("loginToken", tokenValue);
      tokenCookie.setPath("/bookmarks");
      tokenCookie.setMaxAge(168 * 60 * 60);
      httpResponse.addCookie(tokenCookie);
    }
   
    chain.doFilter(httpRequest, httpResponse);
  }
  
  @Override
  public void destroy() {
    // nothing for now
  }

  private Cookie getCookieByName(Cookie[] cookies, String name) {
    if (cookies == null || cookies.length == 0) {
      return null;
    }
    
    Map<String,Cookie> cookieMap = new HashMap<String,Cookie>();
    for (int i=0; i < cookies.length; i++) {
      Cookie cookie = cookies[i];
      cookieMap.put(cookie.getName(), cookie);
    }
    return cookieMap.get(name);
  }
  
  public void setTokenService(TokenService tokenService) {
    this.tokenService = tokenService;
  }
}