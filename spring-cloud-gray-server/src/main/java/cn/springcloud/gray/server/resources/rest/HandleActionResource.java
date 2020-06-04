package cn.springcloud.gray.server.resources.rest;

import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.server.module.HandleModule;
import cn.springcloud.gray.server.module.domain.Handle;
import cn.springcloud.gray.server.module.domain.HandleAction;
import cn.springcloud.gray.server.module.user.AuthorityModule;
import cn.springcloud.gray.server.resources.domain.fo.HandleActionFO;
import cn.springcloud.gray.server.utils.ApiResHelper;
import cn.springcloud.gray.server.utils.PaginationUtils;
import cn.springcloud.gray.server.utils.SessionUtils;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static cn.springcloud.gray.api.ApiRes.CODE_SUCCESS;

/**
 * @author saleson
 * @date 2020-05-31 00:36
 */
@RestController
@RequestMapping("/handle/action")
public class HandleActionResource {
    @Autowired
    private HandleModule handleModule;
    @Autowired
    private AuthorityModule authorityModule;


    @GetMapping(value = "/page")
    public ResponseEntity<ApiRes<List<HandleAction>>> page(
            @RequestParam("handleId") Long handleId,
            @ApiParam @PageableDefault(sort = "order", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<HandleAction> page = handleModule.listHandleActionsByHandleId(handleId, pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(page);
        return new ResponseEntity<>(
                ApiRes.<List<HandleAction>>builder()
                        .code(CODE_SUCCESS)
                        .data(page.getContent())
                        .build(),
                headers,
                HttpStatus.OK);
    }


    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public ApiRes<Void> delete(@PathVariable("id") Long id) {
        ApiRes<Void> apiRes = vaildHandleAction(id);
        if (Objects.nonNull(apiRes)) {
            return apiRes;
        }
        handleModule.deleteHandleAction(id, SessionUtils.currentUserId());
        return ApiRes.<Void>builder().code(CODE_SUCCESS).build();
    }

    @RequestMapping(value = "/{id}/recover", method = RequestMethod.PATCH)
    public ApiRes<Void> recover(@PathVariable("id") Long id) {
        ApiRes<Void> apiRes = vaildHandleAction(id);
        if (Objects.nonNull(apiRes)) {
            return apiRes;
        }
        handleModule.recoverHandle(id, SessionUtils.currentUserId());
        return ApiRes.<Void>builder().code(CODE_SUCCESS).build();
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ApiRes<HandleAction> save(@RequestBody HandleActionFO handleActionFO) {
        ApiRes<HandleAction> apiRes = vaildHandleAuthority(handleActionFO.getHandleId());
        if (Objects.nonNull(apiRes)) {
            return apiRes;
        }
        HandleAction handleAction = handleModule.getHandleAction(handleActionFO.getId());
        if (Objects.isNull(handleAction)) {
            handleAction = new HandleAction();
            handleAction.setId(handleAction.getId());
            handleAction.setDelFlag(false);
        }
        handleActionFO.fillToHandleAction(handleAction);
        handleAction.setOperator(SessionUtils.currentUserId());
        handleAction.setOperateTime(new Date());

        return ApiRes.<HandleAction>builder()
                .code(CODE_SUCCESS)
                .data(handleModule.saveHandleAction(handleAction))
                .build();
    }


    private <T> ApiRes<T> vaildHandleAction(Long handleActionId) {
        HandleAction handleAction = handleModule.getHandleAction(handleActionId);
        if (Objects.isNull(handleAction)) {
            return ApiResHelper.notFound();
        }
        return vaildHandleAuthority(handleAction.getHandleId());
    }


    private <T> ApiRes<T> vaildHandleAuthority(Long handleId) {
        Handle handle = handleModule.getHandle(handleId);
        if (Objects.isNull(handle)) {
            return ApiResHelper.notFound("未找到handle纪录");
        }
        if (!authorityModule.hasNamespaceAuthority(handle.getNamespace())) {
            return ApiResHelper.notAuthority();
        }
        return null;
    }

}
