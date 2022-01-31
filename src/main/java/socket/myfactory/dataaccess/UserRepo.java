package socket.myfactory.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import socket.myfactory.entities.User;

public interface UserRepo extends JpaRepository<User,Long>{
	
	@Query("select u from users u where u.username=?1 and u.password=?2")
	User login(String username,String password);
}
