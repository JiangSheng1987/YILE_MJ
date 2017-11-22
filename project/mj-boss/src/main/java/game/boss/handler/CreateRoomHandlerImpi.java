package game.boss.handler;

import game.boss.model.User;
import game.boss.services.RoomService;
import mj.net.handler.login.CreateRoomHandler;
import mj.net.message.login.CreateRoom;
import mj.net.message.login.OptionEntry;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zuoge85@gmail.com on 16/10/3.
 */
@Component
public class CreateRoomHandlerImpi implements CreateRoomHandler<User> {
    @Autowired
    private RoomService roomService;

    @Override
    public boolean handler(CreateRoom msg, User user) {
    	String profile = msg.getProfile();
    	
    	
    	if(profile.equals("DK")){
    		roomService.proxyCreateRoom(msg, user,1000);
    	}else if(profile.equals("DN")){
    		roomService.createRoom2(msg, user,1003);
    	}else if(profile.equals("DNK")){
    		roomService.proxyCreateRoom(msg, user,1003);
    	}else{
    		roomService.createRoom2(msg, user,1000);
    	}
        return false;
    }
}
