package com.aidp.service.impl;

import com.aidp.entity.Blog;
import com.aidp.mapper.BlogMapper;
import com.aidp.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
