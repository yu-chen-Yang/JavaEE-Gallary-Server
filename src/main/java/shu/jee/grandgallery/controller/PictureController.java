package shu.jee.grandgallery.controller;


import com.UpYun;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upyun.UpException;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shu.jee.grandgallery.entity.data.Picture;
import shu.jee.grandgallery.entity.data.PictureInfo;
import shu.jee.grandgallery.entity.data.Tags;
import shu.jee.grandgallery.request.CommentReq;
import shu.jee.grandgallery.request.UserPictureReq;
import shu.jee.grandgallery.response.Response;
import shu.jee.grandgallery.service.PictureService;
import shu.jee.grandgallery.service.TagsService;
import shu.jee.grandgallery.service.UserService;

import java.io.IOException;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 杨宇辰
 * @since 2021-05-24
 */

@CrossOrigin
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

//    @GetMapping("/getPictures")
//    Response getPictures(String category,Integer page,String method) {
//        if (method == null)
//            return Response.success(null,pictureService.getPictures(category,page,"picture_id","asc"));
//        else if (method.equals("latest"))
//            return Response.success(null,pictureService.getPictures(category,page,"publish_time","desc"));
//        else if (method.equals("hottest"))
//            return Response.success(null,pictureService.getPictures(category,page,"view_time","desc"));
//        else
//            return Response.success(null,pictureService.getPictures(category,page,"picture_id","asc"));
//
//    }
    @GetMapping("/getPictures")
    @ApiOperation(value = "获取图片信息",notes = "" +
            "返回一个列表，除页数外均为选填项，默认排序方式为最多访问;" +
            "排序依据可以是view_time（访问次数）或者publish_time（上传时间），当然也可以是picture_id;" +
            "排序顺序可以是ASC（升序）或者DESC（降序）;" +
            "如果填写了上传者或分类名，则值返回满足条件的图片信息;")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uploaderId", value = "上传者"),
            @ApiImplicitParam(name = "categoryName", value = "分类名"),
            @ApiImplicitParam(name = "orderBy", value = "排序依据"),
            @ApiImplicitParam(name = "order", value = "排序顺序"),
            @ApiImplicitParam(name = "page", value = "页数", required = true)
    })
    Response getPictures(Integer uploaderId,String categoryName,String orderBy,String order,Integer page) {
        if (page == null) return Response.error("page is required");
        List<Picture> pictures = pictureService.getPictures(uploaderId,categoryName,orderBy,order,page);
        List<PictureInfo> pictureInfos = new ArrayList<>();
        for (Picture picture:pictures) {
            pictureInfos.add(pictureService.getInformation(picture.getPictureId()));
        }
        return Response.success(null,pictureInfos);
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

    @PostMapping("/uploadPicture")
    Response uploadPicture(MultipartFile file,String picturename,String categoryname,String description,Integer userid) throws IOException, UpException {
        PictureInfo pictureInfo=new PictureInfo();
        pictureInfo.setPictureName(picturename);
        pictureInfo.setDescription(description);
        pictureInfo.setCategoryName(categoryname);
        pictureInfo.setUploaderId(userid);
        UpYun upYun=new UpYun("grandgallery-image","zjh","Sb1GzBevRLbpcG2WsfOp5JFmmQrQOTLn");
        String filename=file.getOriginalFilename()+ UUID.randomUUID().toString()+".jpg";
        pictureInfo.setPictureUrl("http://grandgallery-image.test.upcdn.net/"+filename);
        System.out.println("图片名称："+filename);
        boolean re = upYun.writeFile(filename,file.getBytes(),false);
        pictureService.addPicture(pictureInfo);
        return Response.success(null);
    }


}

