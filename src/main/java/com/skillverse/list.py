import os

def list_files_tree(folder_path, prefix=""):
    # Get sorted list of entries so output is consistent
    entries = sorted(os.listdir(folder_path))
    entries_count = len(entries)
    
    for index, entry in enumerate(entries):
        full_path = os.path.join(folder_path, entry)
        is_last = (index == entries_count - 1)
        
        # Choose branch or corner characters based on position
        branch = "└── " if is_last else "├── "
        print(prefix + branch + entry)
        
        if os.path.isdir(full_path):
            # Prepare the prefix for the next level
            extension = "    " if is_last else "│   "
            list_files_tree(full_path, prefix + extension)

if __name__ == "__main__":
    folder = input("Enter the path to the folder: ").strip()
    if os.path.isdir(folder):
        print(folder)
        list_files_tree(folder)
    else:
        print("The provided path is not a valid directory.")
