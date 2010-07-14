#import <AppKit/AppKit.h>
//#import <CoreFoundation/CoreFoundation.h>
#import <iostream>
#import "ScopedAutoReleasePool.h"

int
main(int argc, const char *const *argv)
{
        if (argc != 3) {
                std::cerr << "Usage: " << argv[0] << " TITLE MESSAGE" << std::endl;
                return 1;
        }

/*
        CFUserNotificationDisplayAlert(0, 0, NULL, NULL, NULL,
                                       CFSTR("header"), CFSTR("message"), CFSTR("default button"),
                                       CFSTR("alt button"), CFSTR("other button"), NULL);
                                     */

        ScopedAutoReleasePool pool;
        [NSApplication sharedApplication];

        NSRunInformationalAlertPanel([NSString stringWithUTF8String:argv[1]],
                                     [NSString stringWithUTF8String:argv[2]],
                                     nil, nil, nil);

        return 0;
}
