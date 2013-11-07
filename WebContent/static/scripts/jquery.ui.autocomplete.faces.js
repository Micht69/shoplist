$.widget("ui.autocomplete", $.ui.autocomplete, {

    _normalize: function(items) {
        return $.parseJSON(items.text());
    }

});
