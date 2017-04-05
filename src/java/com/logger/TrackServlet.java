/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logger;

import com.datasourse.ControlPanelPool;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author nail yusupov
 */
public class TrackServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        /*
        Pass the id of the user in the request url that is added when the js file is generated
        compare it to the domain selected during the registration, id the incoming request domain name 
        matches the one selected during the registration store data
        The domain verification should happen once per request by verifying the associated user registered domain name
        with the current location domain name and if those are matching proceed to writing the record to the database
        
        in the contact parsing store the domain name of the website where the information was extracted from
        
        also for the verification logic it could work the opposite way, where the domain where the request comes from is ran
        against the database, where the if of the table is looked up and inserted into the remaining lead session requiring statements
        
         */

        if (request.getParameter("q") == null) {
            String sessionId = request.getSession().getId();
            String remoteAddress = request.getRemoteAddr();
            //InetAddress tempAddress = InetAddress.getByName(remoteAddress);
            //String hostName = tempAddress.getHostName();
            String pageTitle = request.getParameter("trackPageTitle");
            String userAgent = request.getParameter("trackUserAgent");
            String tempId = request.getParameter("id");
            //String domain = request.getParameter("trackDomain"); //not needed for now, then use for client domain registration comparison
            String screenHeight = request.getParameter("trackScreenHeight");
            String referer = request.getParameter("trackReferer");
            String screenWidth = request.getParameter("trackScreenWidth");
            String location = request.getParameter("trackLocation");
            java.sql.Connection con = null;
            java.sql.PreparedStatement stmt = null;
            java.sql.ResultSet rs = null;
            try {
                con = ControlPanelPool.getInstance().getConnection();
                stmt = con.prepareStatement("INSERT INTO [dbo].[leadSession] ([sessionId],[remoteAddress],[pageTitle],[referer],[location],[userAgent],[screenHeight],[screenWidth],[text],[temp],[timeIn],[timeOut]) VALUES (?,?,?,?,?,?,?,?,?,?,GETDATE(),?)");
                stmt.setString(1, sessionId);
                stmt.setString(2, remoteAddress);
                stmt.setString(3, pageTitle);
                stmt.setString(4, referer);
                stmt.setString(5, location);
                stmt.setString(6, userAgent);
                stmt.setString(7, screenHeight);
                stmt.setString(8, screenWidth);
                stmt.setString(9, "");
                stmt.setString(10, tempId);
                stmt.setString(11, "");
                stmt.executeUpdate();
                con.close();
            } catch (SQLException | PropertyVetoException ex) {
                Logger.getLogger(TrackServlet.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                DbUtils.closeQuietly(con, stmt, rs);
            }
            //store the company data
            try {
                JSONObject obj = new JSONObject(getJsonData(remoteAddress));
                if (!ipExists(remoteAddress)) {
                    for (int i = 0; i < obj.getJSONObject("data").getJSONArray("records").length(); i++) {
                        JSONArray org = obj.getJSONObject("data").getJSONArray("records").getJSONArray(i);
                        storeIp(remoteAddress, getOrganizationId(org), i == 0);
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(TrackServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            response.setHeader("Cache-control", "no-cache, no-store");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "-1");
            response.setStatus(204);
        }
        if (request.getParameter("q") != null) {
            java.sql.Connection con = null;
            java.sql.PreparedStatement stmt = null;
            try {
                if (request.getParameter("q").length() > 1) {
                    con = ControlPanelPool.getInstance().getConnection();
                    stmt = con.prepareStatement("UPDATE leadSession SET text = ?, timeOut = GETDATE() WHERE temp = ?");
                    stmt.setString(1, request.getParameter("q").substring(1));
                    stmt.setString(2, request.getParameter("id"));
                    stmt.executeUpdate();
                    parseContactInfo(request.getParameter("q"), request.getRemoteAddr(), request.getParameter("trackDomain"), request.getParameter("name"), request.getParameter("email"), request.getParameter("web"), request.getParameter("addr"), request.getParameter("bb"), request.getParameter("phone"));
                } else {
                    con = ControlPanelPool.getInstance().getConnection();
                    stmt = con.prepareStatement("UPDATE leadSession SET timeOut = GETDATE() WHERE temp = ?");
                    stmt.setString(1, request.getParameter("id"));
                    stmt.executeUpdate();
                }
                con.close();
            } catch (SQLException | PropertyVetoException ex) {
                Logger.getLogger(TrackServlet.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                DbUtils.closeQuietly(stmt);
                DbUtils.closeQuietly(con);
            }
            response.setStatus(204);
        }
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String getJsonData(String ip) {
        String result = "";
        try {
            URL url = new URL("https://stat.ripe.net/data/whois/data.json?resource=" + ip);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.connect();
            int status = connection.getResponseCode();
            if (status == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result += line;
                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(TrackServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TrackServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private boolean ipExists(String remoteAddress) {
        boolean exists = false;
        java.sql.Connection con = null;
        java.sql.PreparedStatement stmt = null;
        java.sql.ResultSet rs = null;
        try {
            con = ControlPanelPool.getInstance().getConnection();
            stmt = con.prepareStatement("SELECT ip FROM LeadRemoteAddress WHERE ip = ?");
            stmt.setString(1, remoteAddress);
            rs = stmt.executeQuery();
            while (rs.next()) {
                exists = true;
            }
        } catch (IOException | SQLException | PropertyVetoException ex) {
            Logger.getLogger(TrackServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
        return exists;
    }

    private void storeIp(String remoteAddress, String companyId, boolean prefered) {
        boolean exist = false;
        java.sql.Connection con = null;
        java.sql.PreparedStatement stmt = null;
        java.sql.ResultSet rs = null;
        try {
            con = ControlPanelPool.getInstance().getConnection();
            stmt = con.prepareStatement("SELECT * FROM LeadRemoteAddress WHERE ip = ? AND LeadOrganization_uid = ?");
            stmt.setString(1, remoteAddress);
            stmt.setString(2, companyId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                exist = true;
            }
            if (!exist) {
                stmt = con.prepareStatement("INSERT INTO LeadRemoteAddress (ip, LeadOrganization_uid, prefered) VALUES (?,?,?)");
                stmt.setString(1, remoteAddress);
                stmt.setString(2, companyId);
                stmt.setString(3, prefered ? "1" : "0");
                stmt.executeUpdate();
            }
        } catch (IOException | SQLException | PropertyVetoException ex) {
            Logger.getLogger(TrackServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
    }

    private String getOrganizationId(JSONArray org) {
        String id = "1";
        String name = "";
        String address = "";
        String city = "";
        String stateProv = "";
        String postalCode = "";
        String country = "";
        for (int j = 0; j < org.length(); j++) {
            try {
                JSONObject temp = org.getJSONObject(j);
                if (String.valueOf(temp.get("key")).equalsIgnoreCase("Organization") || String.valueOf(temp.get("key")).equalsIgnoreCase("OrgName") || String.valueOf(temp.get("key")).equalsIgnoreCase("Customer") || String.valueOf(temp.get("key")).equalsIgnoreCase("CustName") || String.valueOf(temp.get("key")).equalsIgnoreCase("owner") || String.valueOf(temp.get("key")).equalsIgnoreCase("descr")) {
                    name = String.valueOf(temp.get("value"));
                }
                if (String.valueOf(temp.get("key")).equalsIgnoreCase("Address")) {
                    address = String.valueOf(temp.get("value"));
                }
                if (String.valueOf(temp.get("key")).equalsIgnoreCase("City")) {
                    city = String.valueOf(temp.get("value"));
                }
                if (String.valueOf(temp.get("key")).equalsIgnoreCase("StateProv")) {
                    stateProv = String.valueOf(temp.get("value"));
                }
                if (String.valueOf(temp.get("key")).equalsIgnoreCase("PostalCode")) {
                    postalCode = String.valueOf(temp.get("value"));
                }
                if (String.valueOf(temp.get("key")).equalsIgnoreCase("Country")) {
                    country = String.valueOf(temp.get("value"));
                }
            } catch (JSONException ex) {
                Logger.getLogger(TrackServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        java.sql.Connection con = null;
        java.sql.PreparedStatement stmt = null;
        java.sql.ResultSet rs = null;
        try {
            con = ControlPanelPool.getInstance().getConnection();
            stmt = con.prepareStatement("SELECT Id FROM LeadOrganization WHERE Name = ?");
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            while (rs.next()) {
                id = rs.getString("Id");
            }
            if (!name.isEmpty() && id.equals("1")) {
                stmt = con.prepareStatement("INSERT INTO [dbo].[LeadOrganization] ([Name],[Address],[City],[StateProv],[PostalCode],[Country]) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, name);
                stmt.setString(2, address);
                stmt.setString(3, city);
                stmt.setString(4, stateProv);
                stmt.setString(5, postalCode);
                stmt.setString(6, country);
                stmt.execute();
                rs = stmt.getGeneratedKeys();
                while (rs.next()) {
                    id = rs.getString(1);
                }
            }
            con.close();
        } catch (IOException | SQLException | PropertyVetoException ex) {
            Logger.getLogger(TrackServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
        return id;
    }

    private void parseContactInfo(String content, String remoteAddr, String source, String name, String email, String website, String addr, String businessName, String phone) {
        if (email == null || email.isEmpty()) {
            String[] data = content.split("\n");
            for (int i = 0; i < data.length; i++) {
                if (isEmail(data[i])) {
                    email = data[i];
                }
            }
        }
        if (email != null && !email.isEmpty() && !email.equals("null")) {
            java.sql.Connection con = null;
            java.sql.PreparedStatement stmt = null;
            java.sql.ResultSet rs = null;
            try {
                con = ControlPanelPool.getInstance().getConnection();
                stmt = con.prepareStatement("SELECT * FROM LeadContact WHERE Email = ? AND RemoteAddress = ?");
                stmt.setString(1, email);
                stmt.setString(2, remoteAddr);
                rs = stmt.executeQuery();
                boolean exist = false;
                while (rs.next()) {
                    exist = true;
                }
                if (!exist) {
                    stmt = con.prepareStatement("INSERT INTO [dbo].[LeadContact] ([Name],[Email],[WebSite],[RemoteAddress],[Address],[Phone],[BusinessName],[Source]) VALUES (?,?,?,?,?,?,?,?)");
                    stmt.setString(1, name==null?"":name);
                    stmt.setString(2, email);
                    stmt.setString(3, website==null?"":website);
                    stmt.setString(4, remoteAddr);
                    stmt.setString(5, addr==null?"":addr);
                    stmt.setString(6, phone==null?"":phone);
                    stmt.setString(7, businessName);
                    stmt.setString(8, source);
                    stmt.executeUpdate();
                }
                con.close();
            } catch (IOException | SQLException | PropertyVetoException ex) {
                Logger.getLogger(TrackServlet.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                DbUtils.closeQuietly(con, stmt, rs);
            }
        }
    }

    private boolean isEmail(String email) {
        Pattern ptr = Pattern.compile("(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)");
        return ptr.matcher(email).matches();
    }

}
