package com.aidp.service.impl;

import com.aidp.entity.BlogComments;
import com.aidp.mapper.BlogCommentsMapper;
import com.aidp.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
