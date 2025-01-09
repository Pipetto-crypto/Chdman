cmake_minimum_required(VERSION 3.22)

project(chdman C CXX)

include(CheckIncludeFile)
include(CheckIncludeFiles)

add_library(utf8proc STATIC "mame/3rdparty/utf8proc/utf8proc.c")
target_compile_options(utf8proc PRIVATE -MP -MP -pipe -Werror -O3 -fno-strict-aliasing -Wno-unknown-pragmas -Wall -Wcast-align -Wformat-security -Wundef -Wwrite-strings -Wno-conversion -Wno-sign-compare -Wno-error=deprecated-declarations -Wno-unused-result -Wno-array-bounds -Wno-error=attributes -Wno-string-concatenation -Wno-shift-overflow -Wno-return-stack-address -Wno-cast-align -Wpointer-arith -Wstrict-prototypes -Wbad-function-cast -Wno-strict-prototypes)
target_compile_definitions(utf8proc PRIVATE NDEBUG CRLF=2 LSB_FIRST FLAC__NO_DLL PUGIXML_HEADER_ONLY LUA_COMPAT_ALL LUA_COMPAT_5_1 LUA_COMPAT_5_2 PTR64=1 UTF8PROC_STATIC)
target_compile_features(utf8proc PRIVATE c_std_99)

file(GLOB EXPAT_SOURCES "mame/3rdparty/expat/lib/*.c")

add_library(expat STATIC ${EXPAT_SOURCES})
target_compile_options(expat PRIVATE -MP -MP -pipe -Werror -O3 -fno-strict-aliasing -Wno-unknown-pragmas -Wcast-align -Wundef -Wwrite-strings -Wno-conversion -Wno-sign-compare -Wno-error=deprecated-declarations -Wno-unused-result -Wno-array-bounds -Wno-error=attributes -Wno-shift-overflow -Wno-string-concatenation -Wno-return-stack-address -Wno-cast-align -Wpointer-arith -Wstrict-prototypes -Wbad-function-cast)
target_compile_definitions(expat PRIVATE NDEBUG CRLF=2 LSB_FIRST FLAC__NO_DLL PUGIXML_HEADER_ONLY LUA_COMPAT_ALL LUA_COMPAT_5_1 LUA_COMPAT_5_2 PTR64=1 HAVE_MEMMOVE HAVE_STDINT_H HAVE_STDLIB_H HAVE_STRING_H DPACKAGE="expat" PACKAGE_BUGREPORT="expat-bugs@libexpat.org" PACKAGE_NAME="expat" PACKAGE_STRING="expat 2.2.10" PACKAGE_TARNAME="expat" PACKAGE_URL="" PACKAGE_VERSION="2.2.10" STDC_HEADERS -DVERSION="2.2.10" XML_CONTEXT_BYTES=1024 XML_DTD XML_NS BYTEORDER=1234 HAVE_DLFCN_H HAVE_FCNTL_H HAVE_MMAP HAVE_SYS_STAT_H HAVE_SYS_TYPES_H HAVE_UNISTD_H XML_DEV_URANDOM)
target_compile_features(expat PRIVATE c_std_99)

file(GLOB ZLIB_SOURCES "mame/3rdparty/zlib/*.c" )

CHECK_INCLUDE_FILE(unistd.h Z_HAVE_UNISTD_H)

add_library(zlib STATIC ${ZLIB_SOURCES})
if (Z_HAVE_UNISTD_H)
    target_compile_options(zlib PRIVATE -MP -MP -pipe -Werror -O3 -fno-strict-aliasing -Wno-unknown-pragmas -Wall -Wcast-align -Wundef -Wwrite-strings -Wno-conversion -Wno-sign-compare -Wno-error=deprecated-declarations -Wno-unused-result -Wno-array-bounds -Wno-error=attributes -Wno-shift-overflow -Wno-string-concatenation  -Wno-return-stack-address -Wno-cast-align -Wpointer-arith -Wstrict-prototypes -Wbad-function-cast -Wno-strict-prototypes -include unistd.h)
else()
    target_compile_options(zlib PRIVATE -MP -MP -pipe -Werror -O3 -fno-strict-aliasing -Wno-unknown-pragmas -Wall -Wcast-align -Wundef -Wwrite-strings -Wno-conversion -Wno-sign-compare -Wno-error=deprecated-declarations -Wno-unused-result -Wno-array-bounds -Wno-error=attributes -Wno-shift-overflow -Wno-string-concatenation  -Wno-return-stack-address -Wno-cast-align -Wpointer-arith -Wstrict-prototypes -Wbad-function-cast -Wno-strict-prototypes)
endif()
target_compile_definitions(zlib PRIVATE NDEBUG CRLF=2 LSB_FIRST FLAC__NO_DLL PUGIXML_HEADER_ONLY LUA_COMPAT_ALL LUA_COMPAT_5_1 LUA_COMPAT_5_2 PTR64=1 ZLIB_CONST)
target_compile_features(zlib PRIVATE c_std_99)

file(GLOB FLAC_SOURCES  "mame/3rdparty/flac/src/libFLAC/*.c")

# Configure config_type.h
check_include_files(inttypes.h INCLUDE_INTTYPES_H)
check_include_files(stdint.h INCLUDE_STDINT_H)
check_include_files(sys/types.h INCLUDE_SYS_TYPES_H)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")
set(SIZE16 int16_t)
set(USIZE16 uint16_t)
set(SIZE32 int32_t)
set(USIZE32 uint32_t)
set(SIZE64 int64_t)
set(USIZE64 uint64_t)

configure_file(ogg/include/ogg/config_types.h.in ogg/include/ogg/config_types.h @ONLY)

add_library(flac STATIC ${FLAC_SOURCES})
target_include_directories(flac PRIVATE "mame/3rdparty/flac/src/libFLAC/include" "ogg/include" "mame/3rdparty/flac/include" ${PROJECT_BINARY_DIR}/ogg/include)
target_compile_options(flac PRIVATE -MP -MP -pipe -O3 -fno-strict-aliasing -Wno-unknown-pragmas -Wcast-align -Wwrite-strings -Wno-conversion -Wno-sign-compare -Wno-error=deprecated-declarations -Wno-unused-result -Wno-array-bounds -Wno-error=attributes -Wno-shift-overflow -Wno-string-concatenation -Wno-return-stack-address -Wno-cast-align -Wpointer-arith -Wstrict-prototypes -Wbad-function-cast -Wno-unused-function -O0)
target_compile_features(flac PRIVATE c_std_99)
if (NOT CMAKE_SYSTEM_NAME MATCHES "ANDROID")
    target_compile_definitions(flac PRIVATE NDEBUG CRLF=2 LSB_FIRST FLAC__NO_DLLDPUGIXML_HEADER_ONLY LUA_COMPAT_ALL LUA_COMPAT_5_1 LUA_COMPAT_5_2 PTR64=1 WORDS_BIGENDIAN=0 FLAC__NO_ASM _LARGEFILE_SOURCE _FILE_OFFSET_BITS=64 FLAC__HAS_OGG=0 HAVE_CONFIG_H=1 HAVE_LROUND SIZE_MAX SIZE_T_MAX)
else()
    if (CMAKE_ANDROID_ARCH_ABI MATCHES "armeabi-v7a")
        target_compile_definitions(flac PRIVATE NDEBUG CRLF=2 LSB_FIRST FLAC__NO_DLLDPUGIXML_HEADER_ONLY LUA_COMPAT_ALL LUA_COMPAT_5_1 LUA_COMPAT_5_2 PTR64=1 WORDS_BIGENDIAN=0 FLAC__NO_ASM _LARGEFILE_SOURCE FLAC__HAS_OGG=0 HAVE_CONFIG_H=1)
    else()
       target_compile_definitions(flac PRIVATE NDEBUG CRLF=2 LSB_FIRST FLAC__NO_DLLDPUGIXML_HEADER_ONLY LUA_COMPAT_ALL LUA_COMPAT_5_1 LUA_COMPAT_5_2 PTR64=1 WORDS_BIGENDIAN=0 FLAC__NO_ASM _LARGEFILE_SOURCE _FILE_OFFSET_BITS=64 FLAC__HAS_OGG=0 HAVE_CONFIG_H=1 HAVE_FSEEKO HAVE_FTELLO)
   endif()
endif()

file(GLOB 7Z_SOURCES "mame/3rdparty/lzma/C/*.c")

add_library(7z STATIC "mame/3rdparty/lzma/C/LzmaEnc.c" "mame/3rdparty/lzma/C/LzmaDec.c" "mame/3rdparty/lzma/C/Lzma2Dec.c" "mame/3rdparty/lzma/C/Lzma2Enc.c" "mame/3rdparty/lzma/C/LzFind.c" "mame/3rdparty/lzma/C/LzFindMt.c" "mame/3rdparty/lzma/C/Threads.c" "mame/3rdparty/lzma/C/CpuArch.c" "mame/3rdparty/lzma/C/LzFindOpt.c")
target_compile_options(7z PRIVATE -MP -MP -pipe -Werror -O3 -fno-strict-aliasing -Wno-unknown-pragmas -Wall -Wcast-align -Wformat-security -Wundef -Wwrite-strings -Wno-conversion -Wno-sign-compare -Wno-error=deprecated-declarations -Wno-unused-result -Wno-array-bounds -Wno-error=attributes -Wno-shift-overflow -Wno-string-concatenation -Wno-return-stack-address -Wno-cast-align -Wpointer-arith -Wstrict-prototypes -Wbad-function-cast -Wno-strict-prototypes -Wno-undef)
target_compile_definitions(7z PRIVATE NDEBUG CRLF=2 LSB_FIRST FLAC__NO_DLL PUGIXML_HEADER_ONLY LUA_COMPAT_ALL LUA_COMPAT_5_1 LUA_COMPAT_5_2 PTR64=1 _7ZIP_PPMD_SUPPPORT _7ZIP_ST)
target_compile_features(7z PRIVATE c_std_99)

file(GLOB CommonSources "mame/3rdparty/zstd/lib/common/*.c")
file(GLOB CompressSources "mame/3rdparty/zstd/lib/compress/*.c")
file(GLOB DecompressSources "mame/3rdparty/zstd/lib/decompress/*.c")
file(GLOB DictBuilderSources "mame/3rdparty/zstd/lib/dictBuilder/*.c")

set(ZSTD_HEADER "mame/3rdparty/zstd/lib/zstd.h")

add_library(zstd STATIC ${CommonSources} ${CompressSources} ${DecompressSources} ${DictBuilderSources} ${ZSTD_HEADER})
target_include_directories(zstd PRIVATE "mame/3rdparty/zstd/lib")
target_compile_options(zstd PRIVATE -MP -MP -pipe -Werror -O3 -fno-strict-aliasing -Wno-unknown-pragmas -Wcast-align -Wundef -Wwrite-strings -Wno-conversion -Wno-sign-compare -Wno-error=deprecated-declarations -Wno-unused-result -Wno-array-bounds -Wno-error=attributes -Wno-shift-overflow -fvisibility=hidden -Wno-string-concatenation -Wno-return-stack-address -Wno-cast-align -Wimplicit-fallthrough -Wno-class-varargs)
target_compile_definitions(zstd PRIVATE ZSTD_MULTITHREAD ZSTD_DISABLE_ASM)

file(GLOB UTILS_SOURCES "mame/src/lib/util/*.cpp")

add_library(utils STATIC ${UTILS_SOURCES} ${ZSTD_HEADER})
target_compile_options(utils PRIVATE -MP -MP -pipe -Werror -O3 -fno-strict-aliasing -Wno-unknown-pragmas -Wcast-align -Wundef -Wwrite-strings -Wno-conversion -Wno-sign-compare -Wno-error=deprecated-declarations -Wno-unused-result -Wno-array-bounds -Wno-error=attributes -Wno-shift-overflow -Wno-string-concatenation -Wno-return-stack-address -Wno-error=tautological-constant-out-of-range-compare -Wno-cast-align -Woverloaded-virtual -Wimplicit-fallthrough -Wno-class-varargs -Wsuggest-override)
target_include_directories(utils PRIVATE "mame/3rdparty" "mame/3rdparty/expat/lib" "mame/3rdparty/zlib" "mame/3rdparty/flac/include" "mame/3rdparty/utf8proc" "mame/src/osd" "mame/src/lib/util" "mame/3rdparty/zstd/lib")
target_compile_definitions(utils PRIVATE NDEBUG CRLF=2 LSB_FIRST FLAC__NO_DLL PUGIXML_HEADER_ONLY LUA_COMPAT_ALL LUA_COMPAT_5_1 LUA_COMPAT_5_2 PTR64=1 UTF8PROC_STATIC)
target_compile_features(utils PRIVATE cxx_std_17)

add_library(ocore_sdl STATIC "mame/src/osd/modules/file/posixdir.cpp" "mame/src/osd/modules/file/posixfile.cpp" "mame/src/osd/modules/file/posixptty.cpp" "mame/src/osd/modules/file/posixsocket.cpp" "mame/src/osd/modules/lib/osdlib_unix.cpp" "mame/src/osd/modules/osdmodule.cpp" "mame/src/osd/osdcore.cpp" "mame/src/osd/osdsync.cpp" "mame/src/osd/strconv.cpp")
target_compile_options(ocore_sdl PRIVATE -MP -MP -pipe -Werror -O3 -fno-strict-aliasing -Wno-unknown-pragmas -Wcast-align -Wundef -Wwrite-strings -Wno-conversion -Wno-sign-compare -Wno-error=deprecated-declarations -Wno-unused-result -Wno-array-bounds -Wno-error=attributes -Wno-shift-overflow -Wno-string-concatenation -Wno-return-stack-address -Wno-cast-align -Woverloaded-virtual -Wimplicit-fallthrough -Wno-class-varargs)
target_include_directories(ocore_sdl PRIVATE "mame/src/emu" "mame/src/osd" "mame/src/lib" "mame/src/lib/util" "mame/src/osd/sdl")
target_compile_definitions(ocore_sdl PRIVATE NDEBUG CRLF=2 LSB_FIRST FLAC__NO_DLL PUGIXML_HEADER_ONLY LUA_COMPAT_ALL LUA_COMPAT_5_1 LUA_COMPAT_5_2 PTR64=1 USE_NETWORK OSD_NET_USE_TAPTUN SDLMAME_X11 USE_XINPUT=1 USE_XINPUT_DEBUG=0 USE_XINPUT_WII_LIGHTGUN_HACK=0 SDLMAME_SDL2=1 OSD_SDL SDLMAME_UNIX _REENTRANT QT_WIDGETS_LIB QT_GUI_LIB QT_CORE_LIB)
target_compile_features(ocore_sdl PRIVATE cxx_std_17)

add_library(chdman SHARED "mame/src/tools/chdman.cpp")
target_compile_options(chdman PRIVATE -MP -MP -pipe -Werror -O3 -fno-strict-aliasing -Wno-unknown-pragmas -Wall -Wcast-align -Wformat-security -Wundef -Wwrite-strings -Wno-conversion -Wno-sign-compare -Wno-error=deprecated-declarations -Wno-unused-result -Wno-array-bounds -Wno-error=attributes -Wno-shift-overflow -Wno-string-concatenation -Wno-return-stack-address -Wno-cast-align -Woverloaded-virtual -Wimplicit-fallthrough -Wno-class-varargs)
target_compile_definitions(chdman PRIVATE NDEBUG CRLF=2 LSB_FIRST FLAC__NO_DLL PUGIXML_HEADER_ONLY LUA_COMPAT_ALL LUA_COMPAT_5_1 LUA_COMPAT_5_2 PTR64=1)
target_include_directories(chdman PRIVATE "mame/src/osd" "mame/src/lib/util" "mame/3rdparty" "mame/3rdparty/libflac/include" "mame/3rdparty/flac/include")
target_compile_features(chdman PRIVATE cxx_std_17)
target_link_options(chdman PRIVATE -Wl,-undefined,dynamic_lookup -ffunction-sections -fdata-sections -Wl,-gc-sections -static-libstdc++)
target_link_libraries(chdman PRIVATE utils dl expat 7z zlib ocore_sdl flac utf8proc zstd)

