// 页面加载完成时...
$(function(){

    //关闭
    $('#btnCancelBase').click(function() {

        showCancelConfirmForEdit(function(){
            closeTab();
        })

    });
//关闭页卡
    function closeTab() {

        tabs.close(tabs.getSelfId(window));
    }
});




