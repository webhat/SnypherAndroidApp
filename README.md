
README
======
This app was made to upload images to [Snypher](http://snypher.com/), this means that some things have been hardcoded.
As Snypher uses WordPress I decided to reuse a wrapper I had already implemented for WP using the WP build-in
class-IXR.php.

REUSING
=======
It is possible to redirect the app to your own WP site, by changing the value of *WebServiceURL* in strings.xml, and
placing the upload script in */snypher/test.php* on your webserver. To change the path you can modify
**ImageUploadTask.doInBackground**


ATTRIBUTION
===========

The basic template for this app comes from blog items by [Vikas Patel](http://www.linkedin.com/in/vikaskanani)

[Android: Image upload activity](http://vikaskanani.wordpress.com/2011/01/29/android-image-upload-activity/)
[Android: Upload image or file using http POST multi-part](http://vikaskanani.wordpress.com/2011/01/11/android-upload-image-or-file-using-http-post-multi-part/)
