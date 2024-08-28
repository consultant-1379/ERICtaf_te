<head>
    <title>Manual Items</title>
</head>

# Manual Items

Manual Items can be specified within schedules. Test Campaign Id's from Test Management System can be applied to a schedule, to produce combined
Allure report with TE executed tests and TMS test Campaigns.

The following tags are required:

* *&lt;test-campaigns&gt;* - this is a list of *&lt;test-campaign&gt;* tags

Additionally, the following attributes are required for the test-campaign tag:

* *id* - this is the ID of the Test Campaign from TMS (you can take it from test campaign URL; e.g.,
having https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTestCampaign/1220 campaign ID is 1220).

```
<schedule...>
    <manual-item>
        <test-campaigns>
            <test-campaign id="123"/>
            <test-campaign id="345"/>
        </test-campaigns>
    </manual-item>
</schedule>
```
