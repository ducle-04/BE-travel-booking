package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.BlogCommentDTO;
import com.travel.travelbooking.dto.BlogDTO;
import com.travel.travelbooking.dto.BlogSummaryDTO;
import com.travel.travelbooking.dto.CreateCommentDTO;
import com.travel.travelbooking.dto.BlogCreateDTO;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BlogService {

    BlogDTO createBlog(BlogCreateDTO dto, MultipartFile thumbnailFile,
                       List<MultipartFile> imageFiles, UserDetails userDetails) throws IOException;

    BlogDTO approveBlog(Long blogId);

    BlogDTO rejectBlog(Long blogId);

    Page<BlogSummaryDTO> getPublishedBlogs(int page, int size);

    BlogDTO getPublishedBlogById(Long id);

    List<BlogSummaryDTO> getRelatedBlogs(Long blogId);

    List<BlogCommentDTO> getComments(Long blogId);

    BlogCommentDTO createComment(Long blogId, CreateCommentDTO dto, UserDetails userDetails);
}