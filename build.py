import os
import subprocess
import shutil
import glob
import platform
import sys
import argparse
import json
import re
import time

PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
CONFIG_FILE = os.path.join(PROJECT_ROOT, 'build_config.json')
GRADLE_PROPERTIES_FILE = os.path.join(PROJECT_ROOT, 'gradle.properties')
DEFAULT_DIST_DIR = os.path.join(PROJECT_ROOT, 'dist')

BUILD_INFO = {
    "base-universal": {
        "dir": os.path.join('build', 'libs'),
        "task": "baseUniversalJar",
        "classifier": "base-universal",
        "variant": "base",
        "platform": "universal",
        "jar_pattern": "{mod_name}-{version}-base-universal.jar"
    },
    "base-puzzle": {
        "dir": os.path.join('src', 'base', 'puzzle', 'build', 'libs'),
        "task": ":src:base:puzzle:shadowJar",
        "classifier": "base-puzzle",
        "variant": "base",
        "platform": "puzzle",
        "jar_pattern": "{mod_name}-{version}-base-puzzle.jar"
    },
    "base-quilt": {
        "dir": os.path.join('src', 'base', 'quilt', 'build', 'libs'),
        "task": ":src:base:quilt:jar",
        "classifier": "base-quilt",
        "variant": "base",
        "platform": "quilt",
        "jar_pattern": "{mod_name}-base-{version}-quilt.jar"
    },
    "cheat-universal": {
        "dir": os.path.join('build', 'libs'),
        "task": "cheatUniversalJar",
        "classifier": "cheat-universal",
        "variant": "cheat",
        "platform": "universal",
        "jar_pattern": "{mod_name}-{version}-cheat-universal.jar"
    },
    "cheat-puzzle": {
        "dir": os.path.join('src', 'cheat', 'puzzle', 'build', 'libs'),
        "task": ":src:cheat:puzzle:shadowJar",
        "classifier": "cheat-puzzle",
        "variant": "cheat",
        "platform": "puzzle",
        "jar_pattern": "{mod_name}-{version}-cheat-puzzle.jar"
    },
    "cheat-quilt": {
        "dir": os.path.join('src', 'cheat', 'quilt', 'build', 'libs'),
        "task": ":src:cheat:quilt:jar",
        "classifier": "cheat-quilt",
        "variant": "cheat",
        "platform": "quilt",
        "jar_pattern": "{mod_name}-cheat-{version}-quilt.jar"
    },
}

STANDARD_CLEAN_FOLDERS_REL = [
    'build',
    os.path.join('src', 'build'),
    os.path.join('src', 'base', 'build'),
    os.path.join('src', 'base', 'common', 'build'),
    os.path.join('src', 'base', 'puzzle', 'build'),
    os.path.join('src', 'base', 'quilt', 'build'),
    os.path.join('src', 'base', 'common', 'src', 'test'),
    os.path.join('src', 'base', 'puzzle', 'src', 'test'),
    os.path.join('src', 'base', 'quilt', 'src', 'test'),
    os.path.join('src', 'cheat', 'build'),
    os.path.join('src', 'cheat', 'common', 'build'),
    os.path.join('src', 'cheat', 'puzzle', 'build'),
    os.path.join('src', 'cheat', 'quilt', 'build'),
    os.path.join('src', 'cheat', 'common', 'src', 'test'),
    os.path.join('src', 'cheat', 'puzzle', 'src', 'test'),
    os.path.join('src', 'cheat', 'quilt', 'src', 'test'),
    os.path.join('src', 'base', 'puzzle', '.gradle'),
    os.path.join('src', 'cheat', 'puzzle', '.gradle')
]
CACHE_CLEAN_FOLDERS_REL = [
    '.gradle'
]

def load_config(config_path, verbose=False):
    defaults = {
        "enable_archiving": True,
        "archive_every_build": True,
        "archive_directory": "build_archive",
        "valid_build_targets": [
            "base-universal", "cheat-universal",
            "base-puzzle", "cheat-puzzle",
            "base-quilt", "cheat-quilt"
        ],
        "build_targets": [
            "base-universal", "cheat-universal"
        ],
        "build_naming_scheme": {
            "base-universal": "${mod_name}-base-${version}-universal.jar",
            "base-puzzle": "${mod_name}-base-${version}-puzzle.jar",
            "base-quilt": "${mod_name}-base-${version}-quilt.jar",
            "cheat-universal": "${mod_name}-cheat-${version}-universal.jar",
            "cheat-puzzle": "${mod_name}-cheat-${version}-puzzle.jar",
            "cheat-quilt": "${mod_name}-cheat-${version}-quilt.jar"
        },
        "custom_copy_paths": []
    }
    config_to_use = defaults.copy()
    if not os.path.exists(config_path):
        if verbose: print(f"Configuration file not found at '{config_path}'. Creating default...")
        try:
            with open(config_path, 'w') as f: json.dump(defaults, f, indent=4)
            if verbose: print(f"Default configuration saved to '{config_path}'.")
        except IOError as e: print(f"Error: Could not create config file '{config_path}': {e}", file=sys.stderr); print("Proceeding with defaults.", file=sys.stderr)
        except Exception as e: print(f"Error creating config file: {e}", file=sys.stderr); print("Proceeding with defaults.", file=sys.stderr)
    else:
        try:
            with open(config_path, 'r') as f: user_config = json.load(f)
            merged_config = defaults.copy()
            merged_config.update(user_config)
            if "build_targets" in user_config:
                 merged_config["build_targets"] = user_config["build_targets"]
            if "build_naming_scheme" in user_config and isinstance(user_config["build_naming_scheme"], dict):
                 merged_config["build_naming_scheme"] = defaults["build_naming_scheme"].copy()
                 merged_config["build_naming_scheme"].update(user_config["build_naming_scheme"])
            if "custom_copy_paths" in user_config:
                 merged_config["custom_copy_paths"] = user_config["custom_copy_paths"]
            config_to_use = merged_config
            if verbose: print(f"Loaded config from {config_path}")
        except Exception as e: print(f"Error loading config from {config_path}: {e}", file=sys.stderr); print("Using defaults.", file=sys.stderr)

    targets = config_to_use.get("build_targets", ["all"])
    if not isinstance(targets, list): targets = ["all"]
    if "all" in targets or not targets:
        config_to_use["effective_build_targets"] = list(BUILD_INFO.keys())
    else:
        config_to_use["effective_build_targets"] = [t for t in targets if t in BUILD_INFO]
        if not config_to_use["effective_build_targets"] and verbose:
             print(f"Warning: No valid build targets specified in {targets}.", file=sys.stderr)

    config_to_use["archive_directory_abs"] = os.path.join(PROJECT_ROOT, config_to_use.get("archive_directory", "build_archive"))
    config_to_use.pop("mod_name", None)
    return config_to_use

def get_mod_name_from_gradle_properties(properties_path, verbose=False):
    mod_name = None
    default_name = "UnknownMod"
    try:
        if not os.path.exists(properties_path):
            if verbose: print(f"Warning: gradle.properties not found at {properties_path}.", file=sys.stderr)
            return default_name
        with open(properties_path, 'r') as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith('#'): continue
                if line.startswith("mod_name") and '=' in line:
                    mod_name = line.split('=', 1)[1].strip()
                    break
        if mod_name:
            processed_name = mod_name.replace(' ', '_')
            if verbose: print(f"Read mod_name '{mod_name}', processed to '{processed_name}'.")
            return processed_name
        else:
            if verbose: print(f"Warning: 'mod_name' key not found in {properties_path}.", file=sys.stderr)
            return default_name
    except Exception as e:
        print(f"Error reading mod_name: {e}", file=sys.stderr)
        return default_name

def get_gradle_executable():
    if platform.system() == "Windows": gradle_command = os.path.join(PROJECT_ROOT, 'gradlew.bat')
    else:
        gradle_command = os.path.join(PROJECT_ROOT, 'gradlew')
        try:
            if os.path.exists(gradle_command): os.chmod(gradle_command, 0o755)
        except OSError as e: print(f"Warning: Could not set permissions on {gradle_command}: {e}", file=sys.stderr)
    return gradle_command

def extract_version_from_gradle_properties(properties_path):
     try:
        with open(properties_path, 'r') as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith('#'): continue
                if line.startswith("mod_version") and '=' in line:
                    return line.split('=', 1)[1].strip()
     except Exception: pass
     return "UNKNOWN"

def format_filename(scheme, mod_name, target_name, version, original_filename):
    variant = BUILD_INFO[target_name]['variant']
    platform_type = BUILD_INFO[target_name]['platform']
    if not version: version = "UNKNOWN"
    if not mod_name: mod_name = "UnknownMod"
    basename = os.path.splitext(original_filename)[0]
    formatted = scheme
    formatted = formatted.replace('${mod_name}', mod_name)
    formatted = formatted.replace('${version}', version)
    formatted = formatted.replace('${variant}', variant)
    formatted = formatted.replace('${platform}', platform_type)
    formatted = formatted.replace('${target}', target_name)
    formatted = formatted.replace('${original_basename}', basename)
    formatted = re.sub(r'[<>:"/\\|?* ]', '_', formatted)
    if not formatted.lower().endswith('.jar'): formatted += '.jar'
    return formatted

def parse_version(version_string):
    if not version_string: return None
    dev_match = re.match(r'^(\d+(\.\d+)*)-Dev(\d+)$', version_string, re.IGNORECASE)
    if dev_match:
        base = dev_match.group(1); num = int(dev_match.group(3))
        return {'is_dev': True, 'base_version': base, 'dev_number': num, 'full_version': version_string}
    release_match = re.match(r'^(\d+(\.\d+)*)$', version_string)
    if release_match:
        return {'is_dev': False, 'base_version': version_string, 'dev_number': None, 'full_version': version_string}
    return {'is_dev': False, 'base_version': version_string, 'dev_number': None, 'full_version': version_string, 'unknown_format': True}

def run_gradle(config, verbose=False):
    gradle_executable = get_gradle_executable()
    if not os.path.exists(gradle_executable): print(f"Error: Could not find Gradle wrapper '{gradle_executable}'.", file=sys.stderr); sys.exit(1)
    targets_to_run = config.get("effective_build_targets", [])
    gradle_tasks = []
    if not targets_to_run:
         print("No valid build targets specified. Skipping Gradle.", file=sys.stderr); return
    is_building_all = (len(targets_to_run) == len(BUILD_INFO))
    if is_building_all:
        gradle_tasks = ['build']
        if verbose: print("Gradle target: build (all targets)")
    else:
        for target_name in targets_to_run:
            if target_name in BUILD_INFO: gradle_tasks.append(BUILD_INFO[target_name]["task"])
        gradle_tasks = list(set(gradle_tasks))
        if verbose: print(f"Gradle targets: {', '.join(gradle_tasks)}")
    gradle_args = gradle_tasks;
    if verbose: gradle_args.append('--info')
    command = [gradle_executable] + gradle_args; print(f"\nRunning Gradle command: {' '.join(command)}" if verbose else "", end="")
    try:
        process = subprocess.Popen(command, cwd=PROJECT_ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, encoding='utf-8', errors='replace')
        stdout, stderr = process.communicate()
        if verbose: print("\n--- Gradle Standard Output ---"); print(stdout); print("--- Gradle Standard Error ---"); print(stderr); print("----------------------------")
        if process.returncode != 0:
            print("\nGRADLE BUILD FAILED", file=sys.stderr); print(f"Command: {' '.join(command)}", file=sys.stderr); print(f"Return Code: {process.returncode}", file=sys.stderr)
            if not verbose: print("\nGradle Output:\n", stdout, file=sys.stderr); print("\nGradle Error:\n", stderr, file=sys.stderr)
            print("-" * 60, file=sys.stderr); sys.exit(1)
        elif verbose: print("\nGradle build successful.")
    except FileNotFoundError: print(f"Error: Could not execute '{gradle_executable}'.", file=sys.stderr); sys.exit(1)
    except Exception as e: print(f"Error running Gradle: {e}", file=sys.stderr); sys.exit(1)

def copy_and_rename_artifacts(config, mod_name, mod_version, verbose=False):
    dist_dir = DEFAULT_DIST_DIR; os.makedirs(dist_dir, exist_ok=True)
    if verbose: print(f"\nProcessing artifacts into: {dist_dir}")
    copied_files_details = []; found_any = False
    naming_schemes = config.get("build_naming_scheme", {})
    custom_copy_rules = config.get("custom_copy_paths", [])
    targets_built = config.get("effective_build_targets", [])
    if not targets_built: print("\nNo targets built, skipping artifact processing."); return []

    for target_name in targets_built:
        target_info = BUILD_INFO.get(target_name)
        if not target_info: continue
        build_lib_dir_rel = target_info["dir"]
        build_lib_dir_abs = os.path.join(PROJECT_ROOT, build_lib_dir_rel)
        if not os.path.isdir(build_lib_dir_abs):
            if verbose: print(f"Dir not found for '{target_name}', skipping: {build_lib_dir_abs}"); continue
        if verbose: print(f"Checking for '{target_name}' JAR in {build_lib_dir_abs}...")

        file_version = mod_version

        expected_jar_name_pattern = target_info["jar_pattern"].format(mod_name=mod_name.replace('_', '-'), version=file_version)
        jar_search_pattern = os.path.join(build_lib_dir_abs, expected_jar_name_pattern)
        jars_found = glob.glob(jar_search_pattern)

        if not jars_found and target_info.get("classifier"):
             fallback_pattern = os.path.join(build_lib_dir_abs, f"*-{target_info['classifier']}.jar")
             if verbose: print(f" Specific JAR not found, trying pattern: {fallback_pattern}")
             jars_found = glob.glob(fallback_pattern)
             jars_found = [j for j in jars_found if '-sources' not in os.path.basename(j) and '-javadoc' not in os.path.basename(j)]

        if not jars_found:
             fallback_pattern_any = os.path.join(build_lib_dir_abs, '*.jar')
             if verbose: print(f" Classifier JAR not found, trying any JAR: {fallback_pattern_any}")
             jars_found = glob.glob(fallback_pattern_any)
             jars_found = [j for j in jars_found if '-sources' not in os.path.basename(j) and '-javadoc' not in os.path.basename(j) and '-plain' not in os.path.basename(j)]

        if jars_found:
            found_any = True
            source_jar_path = jars_found[0]
            if len(jars_found) > 1 and verbose: print(f" Warning: Found multiple JARs for '{target_name}'. Using '{os.path.basename(source_jar_path)}'.")
            original_filename = os.path.basename(source_jar_path);

            scheme = naming_schemes.get(target_name, "${mod_name}-${version}-${target}.jar")
            final_filename = format_filename(scheme, mod_name, target_name, file_version, original_filename);
            final_dist_path = os.path.join(dist_dir, final_filename)

            try:
                if verbose: print(f" Copying '{original_filename}' -> '{final_filename}' to dist/")
                shutil.copy2(source_jar_path, final_dist_path)
                copied_files_details.append({
                    "source_path": source_jar_path, "dist_path": final_dist_path,
                    "target_name": target_name, "variant": target_info['variant'],
                    "platform": target_info['platform'], "version": file_version,
                    "final_filename": final_filename
                })
            except Exception as e: print(f" Error copying {original_filename}: {e}", file=sys.stderr)

            for rule in custom_copy_rules:
                rule_targets = rule.get("targets", []); rule_dest = rule.get("destination")
                if target_name in rule_targets and rule_dest:
                    abs_rule_dest = os.path.abspath(os.path.join(PROJECT_ROOT, rule_dest)) if not os.path.isabs(rule_dest) else rule_dest;
                    custom_dest_path = os.path.join(abs_rule_dest, final_filename)
                    try:
                        os.makedirs(abs_rule_dest, exist_ok=True)
                        if verbose: print(f" Copying '{final_filename}' to custom path: {abs_rule_dest}")
                        shutil.copy2(final_dist_path, custom_dest_path)
                    except Exception as e: print(f" Error copying {final_filename} to custom path {abs_rule_dest}: {e}", file=sys.stderr)
        elif verbose: print(f" No JAR found for '{target_name}'.")

    if not found_any: print(f"\nWarning: No JARs found for targets: {targets_built}", file=sys.stderr)
    elif not copied_files_details: print("\nWarning: Failed to copy any found JARs.", file=sys.stderr)
    elif verbose: print(f"\nProcessed {len(copied_files_details)} artifact(s) into {dist_dir}.")
    return copied_files_details

def archive_build(copied_files_details, config, verbose=False):
    if not config.get("enable_archiving", False):
        if verbose: print("\nArchiving disabled.") ; return
    if not copied_files_details:
        if verbose: print("\nNo artifacts copied, skipping archiving.") ; return
    if verbose: print("\nArchiving build...")
    archive_base_dir_abs = config["archive_directory_abs"]
    archive_every_build = config.get("archive_every_build", False)

    artifacts_by_version = {}
    for details in copied_files_details:
        version = details.get("version", "UNKNOWN_VERSION")
        if version not in artifacts_by_version: artifacts_by_version[version] = []
        artifacts_by_version[version].append(details)
    if not artifacts_by_version: print("\nWarning: Could not determine version. Skipping archiving.", file=sys.stderr); return

    for version_string, artifacts in artifacts_by_version.items():
        if verbose: print(f"\nArchiving for version: {version_string}")
        version_info = parse_version(version_string)
        if not version_info:
             if verbose: print(f" Warning: Version '{version_string}' parse failed. Treating as unknown.", file=sys.stderr)
             version_info = {'is_dev': False, 'base_version': version_string, 'dev_number': None, 'full_version': version_string, 'unknown_format': True}
        base_version_name = version_info['base_version']
        safe_base_version_name = re.sub(r'[<>:"/\\|?* ]', '_', base_version_name)
        version_group_path = os.path.join(archive_base_dir_abs, safe_base_version_name)
        os.makedirs(version_group_path, exist_ok=True)
        if verbose: print(f" Base archive path: {version_group_path}")
        is_dev = version_info['is_dev']

        if is_dev:
            dev_builds_base_path = os.path.join(version_group_path, "dev_builds")
            specific_latest_path = os.path.join(dev_builds_base_path, "latest")
            os.makedirs(dev_builds_base_path, exist_ok=True)
            latest_type_msg = "dev 'latest'"
        else:
            specific_latest_path = os.path.join(version_group_path, "latest")
            latest_type_msg = "release 'latest'" if not version_info.get('unknown_format') else "'latest' for unknown format"

        if verbose: print(f" Updating {latest_type_msg} at: {specific_latest_path}")
        try:
            if os.path.isdir(specific_latest_path): shutil.rmtree(specific_latest_path, ignore_errors=True)
            os.makedirs(specific_latest_path, exist_ok=True)
            copied_count = 0
            for artifact_info in artifacts:
                dest_path = os.path.join(specific_latest_path, artifact_info["final_filename"])
                try: shutil.copy2(artifact_info["dist_path"], dest_path); copied_count += 1
                except Exception as e: print(f" Error copying {artifact_info['final_filename']} to {latest_type_msg}: {e}", file=sys.stderr)
            if verbose: print(f" Updated {copied_count} artifact(s) in {latest_type_msg} for v{version_string}.")
        except Exception as e: print(f" Error updating {latest_type_msg} dir {specific_latest_path}: {e}", file=sys.stderr)

        if archive_every_build:
            if is_dev:
                 if version_info['dev_number'] is not None:
                     try:
                         dev_build_instance_name = str(version_info['dev_number']).zfill(3)
                         specific_dev_build_archive_path = os.path.join(dev_builds_base_path, dev_build_instance_name)
                         if verbose: print(f" Archiving dev build instance {dev_build_instance_name} to: {specific_dev_build_archive_path}")
                         os.makedirs(specific_dev_build_archive_path, exist_ok=True)
                         copied_count = 0
                         for artifact_info in artifacts:
                             dest_path = os.path.join(specific_dev_build_archive_path, artifact_info["final_filename"])
                             try: shutil.copy2(artifact_info["dist_path"], dest_path); copied_count += 1
                             except Exception as e: print(f" Error copying {artifact_info['final_filename']} to dev instance {dev_build_instance_name}: {e}", file=sys.stderr)
                         if verbose: print(f" Archived {copied_count} artifact(s) to dev instance {dev_build_instance_name}.")
                     except Exception as e: print(f" Error archiving dev build instance {version_info['dev_number']}: {e}", file=sys.stderr)
                 else: print(f" Error: Dev build but no dev number for v'{version_string}'. Cannot archive.", file=sys.stderr)

            try:
                all_builds_path = os.path.join(version_group_path, "all_builds")
                os.makedirs(all_builds_path, exist_ok=True)
                timestamp_instance_name = time.strftime("%Y%m%d_%H%M%S") + "_" + str(int(time.time() % 1000)).zfill(3)
                timestamp_archive_path = os.path.join(all_builds_path, timestamp_instance_name)
                build_type_msg = "dev" if is_dev else "release" if not version_info.get('unknown_format') else "unknown format"
                if verbose: print(f" Archiving {build_type_msg} instance to: {timestamp_archive_path}")
                os.makedirs(timestamp_archive_path, exist_ok=True)
                copied_count = 0
                for artifact_info in artifacts:
                    dest_path = os.path.join(timestamp_archive_path, artifact_info["final_filename"])
                    try: shutil.copy2(artifact_info["dist_path"], dest_path); copied_count += 1
                    except Exception as e: print(f" Error copying {artifact_info['final_filename']} to timestamp instance {timestamp_instance_name}: {e}", file=sys.stderr)
                if verbose: print(f" Archived {copied_count} artifact(s) to timestamp instance {timestamp_instance_name} for v{version_string}.")
            except Exception as e: print(f" Error archiving build instance for v{version_string}: {e}", file=sys.stderr)
        elif verbose: print(f" Skipping archiving specific instances for v{version_string}.")

def clean_build_folders(config, phase="Pre-Build", verbose=False, clean_cache=False):
    folders_to_clean_rel = STANDARD_CLEAN_FOLDERS_REL[:]
    if clean_cache:
        if verbose: print(f"[{phase}] Cache cleaning enabled.")
        folders_to_clean_rel.extend(CACHE_CLEAN_FOLDERS_REL)
        clean_type = "build and cache"
    else: clean_type = "build output"
    unique_folders_to_clean_rel = set(folders_to_clean_rel)
    if verbose: print(f"\n[{phase}] Performing {clean_type} cleanup..."); print(f"[{phase}] Targets: {sorted(list(unique_folders_to_clean_rel))}")
    deleted_count = 0; skipped_count = 0; error_count = 0
    for folder_rel_path in sorted(list(unique_folders_to_clean_rel)):
        abs_folder_path = os.path.abspath(os.path.join(PROJECT_ROOT, folder_rel_path))
        if verbose: print(f"[{phase}] Cleaning: {abs_folder_path}")
        if not os.path.exists(abs_folder_path) and not os.path.lexists(abs_folder_path):
            if verbose: print(f" Path does not exist, skipping.")
            skipped_count += 1; continue
        try:
            if os.path.isdir(abs_folder_path) and not os.path.islink(abs_folder_path):
                if verbose: print(f" Deleting tree...")
                shutil.rmtree(abs_folder_path, ignore_errors=True)
                time.sleep(0.1)
                if os.path.exists(abs_folder_path): print(f" [{phase}] ERROR: Failed to delete dir {abs_folder_path}.", file=sys.stderr); error_count += 1
                else:
                    if verbose: print(f" Deleted dir."); deleted_count += 1
            elif os.path.isfile(abs_folder_path) or os.path.islink(abs_folder_path):
                if verbose: print(f" Deleting file/link...")
                os.remove(abs_folder_path)
                time.sleep(0.1)
                if os.path.exists(abs_folder_path): print(f" [{phase}] ERROR: Failed to delete file/link {abs_folder_path}.", file=sys.stderr); error_count += 1
                else:
                    if verbose: print(f" Deleted file/link."); deleted_count += 1
            else:
                if verbose: print(f" Path not file/dir/link, skipping."); skipped_count += 1
        except Exception as e: print(f" [{phase}] ERROR deleting {abs_folder_path}: {e}", file=sys.stderr); error_count += 1;
    if verbose: print(f"\n[{phase}] Cleanup complete. Removed: {deleted_count}, Skipped: {skipped_count}, Errors: {error_count}")
    elif error_count > 0: print(f"\n[{phase}] Cleanup finished with {error_count} error(s).", file=sys.stderr)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Build the Europa Client project.')
    parser.add_argument('-v', '--verbose', action='store_true', help='Enable detailed output.')
    parser.add_argument('--clean', action='store_true', help='Clean build outputs before building.')
    parser.add_argument('-cc', '--clean-cache', action='store_true', help='Clean build outputs AND Gradle cache before and after building.')
    parser.add_argument('--no-post-clean', action='store_true', help='Skip cleanup after the build completes.')
    args = parser.parse_args()
    verbose = args.verbose
    do_post_clean = not args.no_post_clean

    print("Starting build process for Europa Client...")
    config = load_config(CONFIG_FILE, verbose=verbose)
    mod_name = get_mod_name_from_gradle_properties(GRADLE_PROPERTIES_FILE, verbose=verbose)
    mod_version = extract_version_from_gradle_properties(GRADLE_PROPERTIES_FILE)

    if os.path.exists(DEFAULT_DIST_DIR): shutil.rmtree(DEFAULT_DIST_DIR)
    if args.clean_cache or args.clean:
        print("\nExecuting Pre-Build Cleanup...")
        clean_build_folders(config, phase="Pre-Build", verbose=verbose, clean_cache=args.clean_cache)
        print("Pre-Build Cleanup Finished.")

    copied_files_details = []
    if config.get("effective_build_targets"):
        if verbose: print(f"\nStarting Gradle build for targets: {config['effective_build_targets']}...")
        run_gradle(config, verbose=verbose)
        if verbose: print("Copying JARs...")
        copied_files_details = copy_and_rename_artifacts(config, mod_name, mod_version, verbose=verbose)
        if verbose: print("Archiving versions...")
        archive_build(copied_files_details, config, verbose=verbose)
    else:
        print("\nNo valid build targets configured. Skipping Gradle build, artifact processing, and archiving.")

    if do_post_clean:
        should_clean_cache_post = args.clean_cache
        print("\nExecuting Post-Build Cleanup...")
        if verbose: print(f"(Cache clean during post-build: {should_clean_cache_post})")
        clean_build_folders(config, phase="Post-Build", verbose=verbose, clean_cache=should_clean_cache_post)
        print("Post-Build Cleanup Finished.")
    elif verbose:
         print("\nSkipping Post-Build Cleanup as requested by --no-post-clean.")

    print("\nBuild process finished.")