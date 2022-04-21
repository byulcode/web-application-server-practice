package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serial;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.pattern.Util;
import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            //요구사항 1
        	BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        	String line = br.readLine();
        	if (line == null) {
        		return;
        	}
        	
        	String url = HttpRequestUtils.getUrl(line);
        	
        	int contentLength = 0;
        	boolean logined = false;
        	while(!"".equals(line)) {
        		log.debug("header : {}", line);
        		line = br.readLine();
        		if(line.contains("Content-Length")) {
        			contentLength = getContentLength(line);
        		}
        		if(line.contains("Cookie")) {
        			logined = isLogin(line);
        		}
        	}
        	log.debug("Content-Length : {}", contentLength);


        	if(url.startsWith("/user/creat")) {
        		//회원가입 정보를 body에서 읽어와 map으로 변환 후 user객체 생성
        		String requestBody = IOUtils.readData(br, contentLength);
        		log.debug("Request Body : {}", requestBody);
        		Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
        		User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        		log.debug("User : {}", user);
        		DataBase.addUser(user);
        		
        		DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos);
        	} else if(url.equals("/user/login")){
        		String requestBody = IOUtils.readData(br, contentLength);
        		log.debug("Request Body : {}", requestBody);
        		Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
        		log.debug("UserId : {}, password : {}", params.get("userId"), params.get("password"));
        		User user = DataBase.findUserById(params.get("userId"));
        		
        		if(user == null) {
        			log.debug("User not found");
        			responseResource(out, "/user/login_failed.html");
        			return;
        		}
        		if(user.getPassword().equals(params.get("password"))) {
        			log.debug("login success");
        			DataOutputStream dos = new DataOutputStream(out);
            		response302HeaderWithCookie(dos, "logined=true");
        		}else {
        			log.debug("password mismatch");
        			responseResource(out, "/user/login_failed.html");
        		}
        	}else if(url.equals("/user/list")) {
        		if(!logined) {
        			responseResource(out, "/user/login.html");
        			return;
        		}
        		Collection<User> users = DataBase.findAll();
        		StringBuilder sb = new StringBuilder();
        		sb.append("<table border='1'>");
        		for (User user : users) {
        			sb.append("<tr>");
        			sb.append("<td>" + user.getUserId() + "</td>");
        			sb.append("<td>" + user.getName() + "</td>");
        			sb.append("<td>" + user.getEmail() + "</td>");
        			sb.append("</tr>");
        		}
        		sb.append("</table>");
        		byte[] body = sb.toString().getBytes();
        		DataOutputStream dos = new DataOutputStream(out);
        		response200Header(dos, body.length);
        		responseBody(dos, body);
        	}
        	else if(url.endsWith(".css")) {
        		DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200HeaderWithCss(dos, body.length);
                responseBody(dos, body);
        	}else {
        		responseResource(out, url);
        	}
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private boolean isLogin(String line) {
    	String[] headerTokens = line.split(":");
    	Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
    	String value = cookies.get("logined");
    	if(value == null) {
    		return false;
    	}
    	return Boolean.parseBoolean(value);
    }
    
    
    private void responseResource(OutputStream out, String url) throws IOException{
    	DataOutputStream dos = new DataOutputStream(out);
    	byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
    	response200Header(dos, body.length);
    	responseBody(dos, body);
    }
    
    private void response302HeaderWithCookie(DataOutputStream dos, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Set-Cookie: " + cookie + " \r\n");
            dos.writeBytes("Location: /index.html \r\n"); //이동할 url 지정
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos) {
        try {//특정 url로 바로 이동하기 때문에 본문(body) 필요x. 이동할 url만 필요
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html \r\n"); //이동할 url 지정
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response200HeaderWithCss(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private int getContentLength(String line) {
    	String[] headerTokens = line.split(":");
    	return Integer.parseInt(headerTokens[1].trim());
    }
    
}
