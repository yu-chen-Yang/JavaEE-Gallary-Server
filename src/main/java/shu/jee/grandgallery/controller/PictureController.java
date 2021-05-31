package shu.jee.grandgallery.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import shu.jee.grandgallery.entity.data.PictureInfo;
import shu.jee.grandgallery.entity.data.Tags;
import shu.jee.grandgallery.entity.data.User;
import shu.jee.grandgallery.request.CommentReq;
import shu.jee.grandgallery.request.UserPictureReq;
import shu.jee.grandgallery.response.Response;
import shu.jee.grandgallery.service.PictureService;
import shu.jee.grandgallery.service.TagsService;
import shu.jee.grandgallery.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 杨宇辰
 * @since 2021-05-24
 */
@RestController
@RequestMapping("//picture")
public class PictureController {

    @Autowired
    PictureService pictureService;

    @Autowired
    TagsService tagsService;

    @Autowired
    UserService userService;

    @GetMapping("/getInfo")
    Response getInfo(Integer pictureId) {
        PictureInfo info = pictureService.getInformation(pictureId);
        if (info == null) return Response.error(null);
        else return Response.success(null,info);
    }

    @GetMapping("/getTags")
    Response getTags(Integer pictureId) {
        Map<String,Object> qryMap = new HashMap<>();
        qryMap.put("picture_id",pictureId);
        List<Tags> tags = tagsService.listByMap(qryMap);
        List<String> tagNames = new ArrayList<>();
        for (Tags tag:tags) {
            tagNames.add(tag.getTagName());
        }
        return Response.success(null,tagNames);
    }

    @GetMapping("/getPictures")
    Response getPictures(String category,Integer page,String method) {
        if (method == null)
            return Response.success(null,pictureService.getPictures(category,page,"picture_id","asc"));
        else if (method.equals("latest"))
            return Response.success(null,pictureService.getPictures(category,page,"publish_time","desc"));
        else if (method.equals("hottest"))
            return Response.success(null,pictureService.getPictures(category,page,"view_time","desc"));
        else
            return Response.success(null,pictureService.getPictures(category,page,"picture_id","asc"));

    }

    @PostMapping("/comment")
    Response doComment(@RequestBody CommentReq req) {
        if (pictureService.doComment(req.getUserId(),req.getPictureId(),req.getContent())) {
            return Response.success(null);
        }
        else {
            return Response.error(null);
        }
    }

    @PostMapping("/likePicture")
    Response likePicture(@RequestBody UserPictureReq req) {
        pictureService.likePicture(req.getUserId(), req.getPictureId());
        return Response.success(null);
    }

    @PostMapping("/dislikePicture")
    Response dislikePicture(@RequestBody UserPictureReq req) {
        pictureService.dislikePicture(req.getUserId(), req.getPictureId());
        return Response.success(null);
    }

    @PostMapping("/favouritePicture")
    Response favouritePicture(@RequestBody UserPictureReq req) {
        pictureService.favouritePicture(req.getUserId(), req.getPictureId());
        return Response.success(null);
    }

    @PostMapping("/disFavouritePicture")
    Response disFavouritePicture(@RequestBody UserPictureReq req) {
        pictureService.disFavouritePicture(req.getUserId(), req.getPictureId());
        return Response.success(null);
    }

    @GetMapping("/getComment")
    Response getComment(Integer pictureId) {
        return Response.success(null,pictureService.getComment(pictureId));
    }

    @GetMapping("/getPublisher")
    Response getPublisher(Integer pictureId) {
        PictureInfo pictureInfo = pictureService.getInformation(pictureId);
        Integer publisherId = pictureInfo.getUploaderId();
        return Response.success(null,userService.getBasicInfo(publisherId));
    }


}

