package nicetu.kuvarin.news.newsJwtApp.controller;

import nicetu.kuvarin.news.newsJwtApp.exception.*;
import nicetu.kuvarin.news.newsJwtApp.model.Post;
import nicetu.kuvarin.news.newsJwtApp.payload.request.*;
import nicetu.kuvarin.news.newsJwtApp.payload.response.MessageResponse;
import nicetu.kuvarin.news.newsJwtApp.payload.response.PostResponse;
import nicetu.kuvarin.news.newsJwtApp.service.CommentService;
import nicetu.kuvarin.news.newsJwtApp.service.PostService;
import nicetu.kuvarin.news.newsJwtApp.service.ThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private CommentService commentService;


    @GetMapping
    public ResponseEntity<?> getPosts() {
        try{
            return ResponseEntity.ok(postService.getPostResponseList(postService.getAll()));
        } catch (PostsNotFoundException | UserNotAuthorizedException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable String id){
        try{
            Post post=postService.getPostById(id);

            return ResponseEntity.ok(new PostResponse(post.getHeader(),
                    post.getText(),
                    post.getPhoto(),
                    post.getDate(),
                    themeService.getThemesNames(post.getThemes()),
                    post.getLikes())
                    );
        } catch (PostNotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest createPostRequest){
        try{
            postService.createPost(createPostRequest);
            return ResponseEntity.ok(new MessageResponse("Пост успешно создан"));
        } catch (CreatePostException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editPost(@PathVariable String id,@RequestBody EditPostRequest editPostRequest){

        try {
            postService.editPost(id,editPostRequest);
            return  ResponseEntity.ok(new MessageResponse("Пост успешно обновлен"));
        } catch (PostsNotFoundException | UserNotAuthorizedException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePost(@PathVariable String id){
        try {
            postService.deletePost(id);
            return ResponseEntity.ok(new MessageResponse("Пост успешно удален!"));
        } catch (PostNotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }

    }

    @PostMapping("/{id}/likes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> setLike(@PathVariable String id){
        try{
            postService.setLike(id);
            return ResponseEntity.ok(new MessageResponse("Лайк поставлен/убран"));
        }
        catch (PostNotFoundException | UserNotAuthorizedException e){
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable String id){
        try{
            return ResponseEntity.ok(commentService.getCommentResponseList(commentService.getComments(id)));
        }
        catch (CommentNotFoundException e){
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }

    }


    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createComment(@PathVariable String id, @RequestBody CreateCommentRequest createCommentRequest){
        try{
            commentService.createComment(id,createCommentRequest);
            return ResponseEntity.ok(new MessageResponse("Коммент успешно оставлен"));
        }
        catch (UserNotAuthorizedException | PostNotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteComment(@PathVariable String id, @RequestBody DeleteCommentRequest deleteCommentRequest){

        try {
            commentService.deleteComment(id,deleteCommentRequest);
            return ResponseEntity.ok(new MessageResponse("Комментарий успешно удален!"));
        } catch (CommentNotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }

    }

}
