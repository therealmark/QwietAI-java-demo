package io.shiftleft.controller;

import io.shiftleft.model.AuthToken;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Admin checks login
 */
@Controller
public class AdminController {
  private String fail = "redirect:/";

  // helper
private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

private boolean isAdmin(String auth) {
  try {
    // Use JWT with enhanced validation to verify token instead of direct deserialization
    Jws<Claims> claims = Jwts.parserBuilder()
      .setSigningKey(getJwtSigningKey())
      .requireIssuer("your-application-name") // Added issuer validation
      .requireAudience("admin-panel") // Added audience validation
      .build()
      .parseClaimsJws(auth);
    
    // More robust role checking with explicit containsKey check
    if (claims.getBody().containsKey("role")) {
      Integer role = claims.getBody().get("role", Integer.class);
      return role != null && role == AuthToken.ADMIN;
    }
    return false;
  } catch (JwtException ex) {
    logger.warn("JWT token cannot be verified: null", ex.getMessage());
    return false;
  } catch (Exception ex) {
    logger.warn("Auth verification failed: null", ex.getMessage());
    return false;
  }
}


  //
  @RequestMapping(value = "/admin/printSecrets", method = RequestMethod.POST)
  public String doPostPrintSecrets(HttpServletResponse response, HttpServletRequest request) {
    return fail;
  }


  @RequestMapping(value = "/admin/printSecrets", method = RequestMethod.GET)
  public String doGetPrintSecrets(@CookieValue(value = "auth", defaultValue = "notset") String auth, HttpServletResponse response, HttpServletRequest request) throws Exception {

    if (request.getSession().getAttribute("auth") == null) {
      return fail;
    }

    String authToken = request.getSession().getAttribute("auth").toString();
    if(!isAdmin(authToken)) {
      return fail;
    }

    ClassPathResource cpr = new ClassPathResource("static/calculations.csv");
    try {
      byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
      response.getOutputStream().println(new String(bdata, StandardCharsets.UTF_8));
      return null;
    } catch (IOException ex) {
      ex.printStackTrace();
      // redirect to /
      return fail;
    }
  }

  /**
   * Handle login attempt
   * @param auth cookie value base64 encoded
   * @param password hardcoded value
   * @param response -
   * @param request -
   * @return redirect to company numbers
   * @throws Exception
   */
@RequestMapping(value = "/admin/login", method = RequestMethod.POST)
public String doPostLogin(@CookieValue(value = "auth", defaultValue = "notset") String auth, 
                         @RequestBody String password, 
                         HttpServletResponse response, 
                         HttpServletRequest request) throws Exception {
  String succ = "redirect:/admin/printSecrets";

  try {
    // no cookie no fun
    if (!auth.equals("notset")) {
      if(isAdmin(auth)) {
        request.getSession().setAttribute("auth", auth);
        return succ;
      }
    }

    // split password=value
    String[] pass = password.split("=");
    if(pass.length!=2) {
      return fail;
    }
    // compare pass
    if(pass[1] != null && pass[1].length()>0 && pass[1].equals("shiftleftsecret"))
    {
      // Create enhanced JWT token instead of serialized object
      Date now = new Date();
      Date expiration = new Date(now.getTime() + 3600000); // 1 hour expiration
      
      String cookieValue = Jwts.builder()
        .setIssuedAt(now)
        .setExpiration(expiration)
        .setIssuer("your-application-name") // Added issuer claim
        .setAudience("admin-panel") // Added audience claim
        .setSubject("admin-authentication") // Added subject claim
        .setId(UUID.randomUUID().toString()) // Added unique ID/nonce to prevent replay attacks
        .claim("role", AuthToken.ADMIN)
        .signWith(getJwtSigningKey())
        .compact();
      
      // Enhanced cookie security with HttpOnly, Secure, and SameSite flags
      Cookie authCookie = new Cookie("auth", cookieValue);
      authCookie.setHttpOnly(true); // Prevents JavaScript access to the cookie
      authCookie.setSecure(true);   // Ensures cookie is sent only over HTTPS
      authCookie.setPath("/");      // Set cookie path
      
      // Set SameSite attribute (using header as Cookie API doesn't support it directly)
      response.setHeader("Set-Cookie", authCookie.getName() + "=" + authCookie.getValue() + 
                        "; HttpOnly; Secure; SameSite=Strict; Path=" + authCookie.getPath());

      // cookie is lost after redirection
      request.getSession().setAttribute("auth", cookieValue);

      return succ;
    }
    return fail;
  }
  catch (Exception ex)
  {
    logger.error("Login error: null", ex.getMessage());
    ex.printStackTrace();
    // no succ == fail
    return fail;
  }
}

  }

  /**
   * Same as POST but just a redirect
   * @param response
   * @param request
   * @return redirect
   */
  @RequestMapping(value = "/admin/login", method = RequestMethod.GET)
  public String doGetLogin(HttpServletResponse response, HttpServletRequest request) {
    return "redirect:/";
  }
}
