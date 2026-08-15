import gzip
import struct

TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12

class NBTTag:
    def __init__(self, tag_type, value):
        self.tag_type = tag_type
        self.value = value

def write_tag(f, name, tag):
    # write type
    f.write(struct.pack('>B', tag.tag_type))
    # write name
    if name is not None:
        name_bytes = name.encode('utf-8')
        f.write(struct.pack('>H', len(name_bytes)))
        f.write(name_bytes)
    write_payload(f, tag)

def write_payload(f, tag):
    t = tag.tag_type
    v = tag.value
    if t == TAG_BYTE:
        f.write(struct.pack('>b', v))
    elif t == TAG_SHORT:
        f.write(struct.pack('>h', v))
    elif t == TAG_INT:
        f.write(struct.pack('>i', v))
    elif t == TAG_LONG:
        f.write(struct.pack('>q', v))
    elif t == TAG_FLOAT:
        f.write(struct.pack('>f', v))
    elif t == TAG_DOUBLE:
        f.write(struct.pack('>d', v))
    elif t == TAG_STRING:
        str_bytes = v.encode('utf-8')
        f.write(struct.pack('>H', len(str_bytes)))
        f.write(str_bytes)
    elif t == TAG_LIST:
        elem_type, elem_list = v
        f.write(struct.pack('>B', elem_type))
        f.write(struct.pack('>i', len(elem_list)))
        for elem in elem_list:
            write_payload(f, elem)
    elif t == TAG_COMPOUND:
        for k, subtag in v.items():
            write_tag(f, k, subtag)
        f.write(struct.pack('>B', TAG_END))
    elif t == TAG_INT_ARRAY:
        f.write(struct.pack('>i', len(v)))
        for x in v:
            f.write(struct.pack('>i', x))

def NBT_Int(val): return NBTTag(TAG_INT, val)
def NBT_String(val): return NBTTag(TAG_STRING, val)
def NBT_Compound(val): return NBTTag(TAG_COMPOUND, val)
def NBT_List(elem_type, val): return NBTTag(TAG_LIST, (elem_type, val))

print("NBT encoder ready")
