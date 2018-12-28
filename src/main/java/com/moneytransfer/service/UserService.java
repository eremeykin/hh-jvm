package com.moneytransfer.service;

import com.moneytransfer.dao.DAOFactory;
import com.moneytransfer.exception.CustomException;
import com.moneytransfer.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserService {

  private final DAOFactory daoFactory = DAOFactory.getDAOFactory(DAOFactory.FAST);
  private final static Pattern EMAIL = Pattern.compile("(?=.{1,250}$)(.+)@(.+){2,}\\.(.+){2,}");
  private static Logger log = Logger.getLogger(UserService.class);


  /**
   * Find by userName
   *
   * @param userName
   * @return
   * @throws CustomException
   */
  @GET
  @Path("/{userName}")
  public User getUserByName(@PathParam("userName") String userName) throws CustomException {
    if (log.isDebugEnabled())
      log.debug("Request Received for get User by Name " + userName);
    final User user = daoFactory.getUserDAO().getUserByName(userName);
    if (user == null) {
      throw new WebApplicationException("User Not Found", Response.Status.NOT_FOUND);
    }
    return user;
  }

  /**
   * Find all
   *
   * @return
   * @throws CustomException
   */
  @GET
  @Path("/all")
  public Response getAllUsers() throws CustomException {
    List<User> users = daoFactory.getUserDAO().getAllUsers();
    StringBuilder sb = new StringBuilder("[");
    for (User u : users){
      sb.append(u);
      sb.append(",");
    }
    sb.deleteCharAt(sb.length()-1);
    sb.append("]");
    return Response.ok(sb.toString()).build();
  }

  /**
   * Create User
   *
   * @param user
   * @return
   * @throws CustomException
   */
  @POST
  @Path("/create")
  public User createUser(User user) throws CustomException {
    Matcher matcher = EMAIL.matcher(user.getEmailAddress());
    if (!matcher.find()) {
      throw new WebApplicationException("User email is in wrong pattern", Response.Status.BAD_REQUEST);
    }
    // TODO should be in one transaction
    if (daoFactory.getUserDAO().getUserByName(user.getUserName()) != null) {
      throw new WebApplicationException("User name already exist", Response.Status.BAD_REQUEST);
    }
    final long uId = daoFactory.getUserDAO().insertUser(user);
    return daoFactory.getUserDAO().getUserById(uId);
  }

  /**
   * Find by User Id
   *
   * @param userId
   * @param user
   * @return
   * @throws CustomException
   */
  @PUT
  @Path("/{userId}")
  public Response updateUser(@PathParam("userId") long userId, User user) throws CustomException {
    final int updateCount = daoFactory.getUserDAO().updateUser(userId, user);
    if (updateCount == 1) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  /**
   * Delete by User Id
   *
   * @param userId
   * @return
   * @throws CustomException
   */
  @DELETE
  @Path("/{userId}")
  public Response deleteUser(@PathParam("userId") long userId) throws CustomException {
    int deleteCount = daoFactory.getUserDAO().deleteUser(userId);
    if (deleteCount == 1) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

//  static {
//    InputStream r = UserService.class.getClassLoader().getResourceAsStream("user.jpg");
//
//    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//
//    int nRead;
//    byte[] data = new byte[16384];
//
//    try {
//      while ((nRead = r.read(data, 0, data.length)) != -1) {
//        buffer.write(data, 0, nRead);
//      }
//    } catch (Exception e) {}
//
//    arr = buffer.toByteArray();
//  }

  @GET
  @Path("/{userId}/image")
  @Produces(MediaType.APPLICATION_SVG_XML)
  public Response getUserImage(@PathParam("userId") long userId) throws IOException {
    InputStream r = UserService.class.getClassLoader().getResourceAsStream("user.jpg");

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = r.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }
    byte[] arr = buffer.toByteArray();
    return Response.ok(arr).header("hash", calcHashImg(arr)).build();
  }

  private int calcHashImg(byte[] img) {
    return Arrays.hashCode(img);
  }
}
