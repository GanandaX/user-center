package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.band.usercenter.model.domain.User;
import generator.service.UserService;
import com.band.usercenter.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author GrandBand
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2023-01-07 16:28:54
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




