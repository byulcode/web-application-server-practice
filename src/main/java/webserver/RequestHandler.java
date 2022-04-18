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
    private DataBase db;

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
        	Map<String, String> headers = new HashMap<>();
        	while(!"".equals(line)) {
        		log.debug("header : {}", line);
        		line = br.readLine();
        		String[] headerTokens = line.split(": ");
        		if(headerTokens.length == 2) {
        			headers.put(headerTokens[0], headerTokens[1]);
        		}
        	}
        	log.debug("Content-Length : {}", headers.get("Content-Length"));


        	if(url.startsWith("/user/creat")) {
        		//회원가입 정보를 body에서 읽어와 map으로 변환 후 user객체 생성
        		String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
        		log.debug("Request Body : {}", requestBody);
        		Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
        		User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        		log.debug("User : {}", user);
        		
        		db.addUser(user);
        		
        		DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos);
        	} else if(url.equals("/user/login")){
        		String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
        		log.debug("Request Body : {}", requestBody);
        		Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
        		log.debug("UserId : {}, password : {}", params.get("userId"), params.get("password"));
        		User user = db.findUserById(params.get("userId"));
        		if(user == null) {
        			log.debug("User not found");
        			DataOutputStream dos = new DataOutputStream(out);
            		response302Header(dos);
        		}else if(user.getPassword().equals(params.get("password"))) {
        			log.debug("login success");
        			DataOutputStream dos = new DataOutputStream(out);
            		response302Header(dos);
        		}else {
        			log.debug("password mismatch");
        			DataOutputStream dos = new DataOutputStream(out);
            		response302HeaderWithCookie(dos, "logined=false");
        		}
        	}else if(url.endsWith(".css")) {
        		DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200HeaderWithCss(dos, body.length);
                responseBody(dos, body);
        	}else {
        		DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
        	}
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302HeaderWithCookie(DataOutputStream dos, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location : /index.html\r\n"); //이동할 url 지정
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos) {
        try {//특정 url로 바로 이동하기 때문에 본문(body) 필요x. 이동할 url만 필요
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location : /index.html\r\n"); //이동할 url 지정
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
}
