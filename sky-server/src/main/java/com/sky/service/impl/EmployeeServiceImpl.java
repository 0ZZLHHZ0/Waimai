package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {



    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对明文密码进行MD5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */

    public void save(EmployeeDTO employeeDTO) {
        System.out.println("当前线程ID" + Thread.currentThread().getId());
        Employee employee = new Employee();

        //对象数据拷贝
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);

        //设置创建时间和更新时间

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //设置密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //当前记录常见ID 和 更新ID
        //已经修改为动态ID
       // employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateUser(BaseContext.getCurrentId());


        employeeMapper.insert(employee);

    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //select * from employee limit 0,10
        //用插件开始查询
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total,records);//同样是根据线程ThreadLocal实现的
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        //update employee set status = ? where id = ?
//        Employee employee = new Employee();
//        employee.setStatus(status);
//        employee.setId(id);
        //也可以用builder

        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);

    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("*****");
        return employee;
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    public void  update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);//beanutils很好用
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(BaseContext.getCurrentId());//底层使用的是线程存储
        employeeMapper.update(employee);



    }
}
