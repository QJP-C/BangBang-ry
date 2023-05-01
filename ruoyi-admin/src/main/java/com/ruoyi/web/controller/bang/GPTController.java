package com.ruoyi.web.controller.bang;

import com.ruoyi.bang.common.R;
import com.ruoyi.bang.utils.GPTUtil;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "GPT接口", value = "GPT接口")
@RestController
@CrossOrigin
@RequestMapping("bang/gpt")
public class GPTController {
    @PostMapping()
    public R get(@RequestBody Map<String,String> map){
        String text = GPTUtil.sendPost(map.get("msg"));
        System.out.println(text);
        return R.success(text);
//        JsonData jsonData = JsonData.bulidSuccess(text);
//        return jsonData;
    }
}
