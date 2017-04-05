# Logger

Logger is the data collector for Lead (https://github.com/nailyusupov/Lead)

****************************************************************************************************
TO ADD NEW SITES TO LEAD TRACKING
****************************************************************************************************

1. Create new javascript file in js folder.

2. Add the file to your site footer.

<script type="text/javascript" src="http://your.host.com/logger/js/filename.js"></script>

3. Define any custom parsing rules following the existing template.


****************************************************************************************************
This logger has been developed to support a single site, but has all features to support multiple
sites. Make minor changes to queries and middleware to enable that feature.

1. Create backend structure that maps new javascript files to new servers (site names).

2. Make sure request for a javascript file is coming from aserver that has been predefined to use
that file to avoid Logger writing incorrect data.

3. Display front end data by source, verify if the logged in user is the owner of the site by 
adding backend structure mapping step 1 server names to registered users.
