function initialize(binding) {
    this.binding = binding;

    binding.getRefreshIntervalMs(function (r) {
        var refreshIntervalMs = r.responseObject();
        console.log("Refresh  " + refreshIntervalMs);

        setInterval(function () {
            binding.getMessages(function (t) {
                var json = t.responseObject();
                console.log(json);
                for (var i = 0; i < json.length; i++) {
                    notificationBar.show(json[i].text, notificationBar[json[i].type]);
                }
            });
        }, refreshIntervalMs);
    })
}

