package main;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Well on 19.02.2016.
 */
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class BackGround {
    @Schedule(hour="*", minute="*", second="0", persistent=false)
    public void WorldMove() throws SQLException, NamingException {
        MyUtils.Logwrite("BackGround","Started");
        World world = new World();
        world.moveFast();
        world.close();
        MyUtils.Logwrite("BackGround","Finished");
    }
    @Schedule(hour="*", minute="0", second="10", persistent=false)
    public void WorldMoveHour() throws SQLException, NamingException {
        MyUtils.Logwrite("BackGround.Hour","Started");
        World world = new World();
        world.moveHour();
        world.close();
        MyUtils.Logwrite("BackGround.Hour","Finished");
    }
    @Schedule(hour="*/4", minute="0", second="20", persistent=false)
    public void WorldSpawn() throws SQLException, NamingException {
        MyUtils.Logwrite("BackGround.Spawn","Started");
        World world = new World();
        world.spawn();
        world.close();
        MyUtils.Logwrite("BackGround.Spawn","Finished");
    }
    @Schedule(dayOfWeek ="3", persistent=false)
    public void TruncateLogs() throws SQLException, NamingException {
        MyUtils.Logwrite("BackGround.TruncateLogs","Started");
        MyUtils.clearLogs();
        MyUtils.Logwrite("BackGround.TruncateLogs","Finished");
    }

    @Schedule(dayOfWeek ="4", persistent=false)
    public void deleteMessages() throws SQLException, NamingException {
        MyUtils.Logwrite("BackGround.deleteMessages","Started");
        MyUtils.deleteMessages();
        MyUtils.Logwrite("BackGround.deleteMessages","Finished");
    }

    @Schedule(hour="*", minute="11", second="37", persistent=false)
    public void generateChests() throws SQLException, NamingException {
        MyUtils.Logwrite("BackGround.generateChests","Started");
        World world = new World();
        world.generateChests();
        world.close();
        MyUtils.Logwrite("BackGround.generateChests","Finished");
    }

    @Schedule(dayOfWeek ="*", persistent=false)
    public void removeOldChests() throws SQLException, NamingException {
        MyUtils.Logwrite("BackGround.removeOldChests","Started");
        World world = new World();
        world.removeOldChests();
        world.close();
        MyUtils.Logwrite("BackGround.removeOldChests","Finished");
    }

    @Schedule(hour="21", minute="17", second="27", persistent=false)
    public void improveBounty() throws SQLException, NamingException {
        MyUtils.Logwrite("BackGround.improveBounty","Started");
        World world = new World();
        world.improveBounty();
        world.close();
        MyUtils.Logwrite("BackGround.improveBounty","Finished");
    }

}
