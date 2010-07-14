#import <AppKit/AppKit.h>
#import <iostream>
#import "ScopedAutoReleasePool.h"

int
main(int argc, const char *const *argv)
{
        if (argc != 3) {
                std::cerr << "Usage: " << argv[0] << " TITLE MESSAGE" << std::endl;
                return 1;
        }

        ScopedAutoReleasePool pool;
        [NSApplication sharedApplication];

        NSRunInformationalAlertPanel([NSString stringWithUTF8String:argv[1]],
                                     [NSString stringWithUTF8String:argv[2]],
                                     nil, nil, nil);

        return 0;
}
