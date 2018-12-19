$(function(){
    //校验
    banger.verify('#modeName',[
        {name: 'required', tips: '评分模型必须填写'},
        {name: 'specialAccount', tips: '评分模型必须由中英文、数字或下划线组成'},
        checkModelNameRule]);

    $('#btnSave').click(function() {
        updateModelScoreInfo();
    });
    //关闭
    $('#btnCancel').click(function() {
        var dialog = getDialog('modelScoreInfoUpdate');
        dialog.close();
    });

});
function updateModelScoreInfo(){
    if (!banger.verify('.ui-form-fields')) {
        return false;
    }
    var postJson = getPostFields();
    jQuery.ajax({
        type : 'post',
        url : '../modelScoreInfo/updateModelScoreInfo.html',
        data : postJson,
        success : function() {
            showConfirm({
                icon: 'succeed',
                content: '更新成功'
            });
            closeDialog();
        }
    });
}

//名字合法性校验  (长度校验 重复性校验)
var checkModelNameRule = {
    name : 'checkModelNameRule',
    tips : '评分模型已经存在！',
    rule : function() {
        var rule = this, bool = true, data = getPostFields();
        var url = '../modelScoreInfo/checkModelNameUpdate.html';
        jQuery.ajax({
            type : 'post',
            url : url,
            dataType:'json',
            async : false,
            data : data,
            success : function(result) {
                bool = result.success;
            }
        });
        return bool;
    }
};
//关闭dialog
function closeDialog(){
    var dialog = getDialog('modelScoreInfoUpdate');
    var win = tabs.getIframeWindow("modelScoreInfo");
    win.location.reload(true);
    setTimeout(function() {
        dialog.close();
    }, 0);
}